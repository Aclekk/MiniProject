package com.example.miniproject.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.R
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.AddReviewRequest
import com.example.miniproject.databinding.FragmentOrderDetailBinding
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val TAG = "ORDER_DETAIL"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated() HIT ✅")

        val orderId = arguments?.getInt("orderId") ?: 0
        Log.d(TAG, "ARGS orderId=$orderId")

        if (orderId == 0) {
            Toast.makeText(requireContext(), "Order ID tidak valid", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        val order = CartManager.orders.find { it.id == orderId }
        Log.d(TAG, "LOCAL order found? ${order != null}")

        if (order == null) {
            Toast.makeText(requireContext(), "Order tidak ditemukan di lokal", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        Log.d(TAG, "LOCAL order status=${order.status} hasReview=${order.hasReview}")

        // Render awal dari lokal
        renderOrder(order)

        // Refresh dari server saat halaman kebuka
        reloadOrderDetailFromServer(orderId) { latestStatus ->
            Log.d(TAG, "SERVER latestStatus=$latestStatus -> set to local")
            order.status = latestStatus
            renderOrder(order)
        }

        binding.btnNextStatus.setOnClickListener {
            Log.d(TAG, "BTN btnNextStatus CLICKED | currentStatus=${order.status}")

            when (order.status.lowercase()) {
                "dikirim", "shipped" -> {
                    Log.d(TAG, "FLOW shipped -> update status to completed FIRST, then open review dialog")

                    // ✅ UPDATE STATUS DULU
                    markOrderCompletedToServer(order.id) { newStatus ->
                        // Update lokal
                        order.status = newStatus
                        renderOrder(order)

                        Toast.makeText(requireContext(), "✅ Status diperbarui jadi Selesai", Toast.LENGTH_SHORT).show()

                        // ✅ BARU BUKA DIALOG REVIEW
                        if (!order.hasReview) {
                            fetchOrderItemProductIds(order.id) { pids ->
                                showRatingDialog(order.id, pids)
                            }
                        }
                    }
                }

                "selesai", "completed" -> {
                    Log.d(TAG, "FLOW completed -> open review dialog")
                    if (!order.hasReview) {
                        fetchOrderItemProductIds(order.id) { pids ->
                            showRatingDialog(order.id, pids)
                        }
                    }
                }

                else -> {
                    Toast.makeText(requireContext(), "Aksi tidak tersedia untuk status ini", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnRateNow.setOnClickListener {
            Log.d(TAG, "BTN btnRateNow CLICKED | hasReview=${order.hasReview}")
            if (!order.hasReview) {
                val pids = order.products.map { it.id }
                Log.d(TAG, "PRODUCT_IDS_FROM_LOCAL=${pids.joinToString(",")}")
                showRatingDialog(order.id, pids)
            }
        }
    }

    private fun renderOrder(order: com.example.miniproject.data.Order) {
        Log.d(TAG, "renderOrder() status=${order.status} hasReview=${order.hasReview}")

        binding.tvOrderId.text = "Order #${order.id}"
        binding.tvOrderStatus.text = "Status: ${order.status}"
        binding.tvOrderPayment.text = "Metode Pembayaran: ${order.paymentMethod}"
        binding.tvOrderAddress.text = "Alamat: ${order.address}"

        val productList = order.products.joinToString("\n") {
            "- ${it.name} (Rp ${String.format("%,d", it.price.toInt())})"
        }
        binding.tvOrderProducts.text = "Produk:\n$productList"
        binding.tvOrderTotal.text = "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        updateButtonVisibility(order)
    }

    private fun updateButtonVisibility(order: com.example.miniproject.data.Order) {
        val st = order.status.lowercase()
        Log.d(TAG, "updateButtonVisibility() st=$st")

        when {
            (st == "dikirim" || st == "shipped") -> {
                binding.btnNextStatus.visibility = View.VISIBLE
                binding.btnNextStatus.text = "✅ Pesanan Telah Sampai"
                binding.btnRateNow.visibility = View.GONE
            }

            (st == "selesai" || st == "completed") && !order.hasReview -> {
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.VISIBLE
                binding.btnRateNow.text = "⭐ Beri Penilaian"
            }

            order.hasReview -> {
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.GONE
            }

            else -> {
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.GONE
            }
        }
    }

    /**
     * ✅ Ambil product_id dari order_items via GET order detail
     * Ini buat ngehindarin mismatch ID lokal vs ID item order di server
     */
    private fun fetchOrderItemProductIds(orderId: Int, onDone: (List<Int>) -> Unit) {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token kosong. Login ulang dulu.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.getOrderDetail(
                    token = "Bearer $token",
                    orderId = orderId
                )

                val body = resp.body()
                val err = resp.errorBody()?.string()

                Log.d("ORDER_PID", "RESP code=${resp.code()} body=$body err=$err")

                if (!resp.isSuccessful || body?.success != true) {
                    Toast.makeText(requireContext(), "Gagal ambil item order", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val data = body.data
                if (data == null) {
                    Toast.makeText(requireContext(), "Data detail order null", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val items = data.items // items default emptyList() harusnya aman
                val productIds = items.mapNotNull { it.productId }

                Log.d("ORDER_PID", "items=${items.size} productIds=$productIds rawItems=$items")

                if (productIds.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "product_id kosong dari server. Ini berarti mapping JSON kamu mismatch atau backend kirim null.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                onDone(productIds)

            } catch (e: Exception) {
                Log.e("ORDER_PID", "EXCEPTION", e)
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**
     * GET order terbaru dari server
     */
    private fun reloadOrderDetailFromServer(orderId: Int, onDone: (String) -> Unit) {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        Log.d(TAG, "reloadOrderDetailFromServer() tokenNullOrEmpty=${token.isNullOrEmpty()}")

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token kosong. Login ulang dulu.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("ORDER_RELOAD", "GET orders/detail.php?id=$orderId tokenPrefix=${token.take(12)}...")

                val resp = ApiClient.apiService.getOrderDetail(
                    token = "Bearer $token",
                    orderId = orderId
                )

                val bodyStr = resp.body()?.toString()
                val errStr = resp.errorBody()?.string()

                Log.d("ORDER_RELOAD", "RESP code=${resp.code()} body=$bodyStr err=$errStr")

                if (resp.isSuccessful && resp.body()?.success == true) {
                    val latestStatus = resp.body()
                        ?.data
                        ?.order
                        ?.status
                        ?.trim()
                        ?.lowercase()

                    Log.d("ORDER_RELOAD", "PARSED latestStatus=$latestStatus")

                    if (latestStatus.isNullOrEmpty()) return@launch

                    val idx = CartManager.orders.indexOfFirst { it.id == orderId }
                    Log.d("ORDER_RELOAD", "LOCAL index=$idx")

                    if (idx != -1) CartManager.orders[idx].status = latestStatus

                    onDone(latestStatus)
                }

            } catch (e: Exception) {
                Log.e("ORDER_RELOAD", "EXCEPTION", e)
            }
        }
    }

    /**
     * Update status completed ke backend
     */
    private fun markOrderCompletedToServer(orderId: Int, onDone: (String) -> Unit) {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        Log.d(TAG, "markOrderCompletedToServer() tokenNullOrEmpty=${token.isNullOrEmpty()}")

        if (token.isNullOrEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val body: Map<String, String> = mapOf(
                    "order_id" to orderId.toString(),
                    "status" to "completed"
                )

                Log.d("ORDER_COMPLETE", "REQ body=$body tokenPrefix=${token.take(12)}...")

                val resp = ApiClient.apiService.updateOrderStatus(
                    token = "Bearer $token",
                    body = body
                )

                val bodyStr = resp.body()?.toString()
                val errStr = resp.errorBody()?.string()

                Log.d("ORDER_COMPLETE", "RESP code=${resp.code()} body=$bodyStr err=$errStr")

                if (resp.isSuccessful && resp.body()?.success == true) {
                    onDone("completed")
                }
            } catch (e: Exception) {
                Log.e("ORDER_COMPLETE", "EXCEPTION", e)
            }
        }
    }

    private fun showRatingDialog(orderId: Int, productIds: List<Int>) {
        Log.d(TAG, "showRatingDialog() orderId=$orderId productIds=${productIds.joinToString(",")}")

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etComment)

        AlertDialog.Builder(requireContext())
            .setTitle("Beri Penilaian untuk Order #$orderId")
            .setView(dialogView)
            .setPositiveButton("Kirim") { _, _ ->
                val ratingFloat = ratingBar.rating
                val rating = ratingFloat.toInt()
                val comment = etComment.text.toString().trim().ifEmpty { "Tidak ada komentar" }

                Log.d(TAG, "DIALOG_SUBMIT clicked | ratingFloat=$ratingFloat rating=$rating comment=$comment")

                if (ratingFloat <= 0f) {
                    Toast.makeText(requireContext(), "Rating minimal 1 bintang ⭐", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                submitReviewsToApi(orderId, productIds, rating, comment)
            }
            .setNegativeButton("Batal") { _, _ ->
                Log.d(TAG, "DIALOG cancelled")
            }
            .show()
    }

    /**
     * Submit review -> kalau sukses -> update status completed -> update UI lokal
     */
    private fun submitReviewsToApi(orderId: Int, productIds: List<Int>, rating: Int, comment: String) {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        Log.d(
            TAG,
            "submitReviewsToApi() HIT ✅ tokenNullOrEmpty=${token.isNullOrEmpty()} orderId=$orderId pids=${productIds.joinToString(",")}"
        )

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token kosong. Login ulang dulu.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val authHeader = "Bearer $token"

                for (pid in productIds) {
                    val req = AddReviewRequest(
                        orderId = orderId,
                        productId = pid,
                        rating = rating.coerceIn(1, 5),
                        comment = comment,
                        isAnonymous = 0
                    )

                    Log.d("REVIEW_ADD", "REQ $req")

                    val res = ApiClient.apiService.addReview(authHeader, req)

                    val bodyStr = res.body()?.toString()
                    val errStr = res.errorBody()?.string()

                    Log.d("REVIEW_ADD", "RESP code=${res.code()} body=$bodyStr err=$errStr")

                    if (!res.isSuccessful || res.body()?.success != true) {
                        val serverMsg = res.body()?.message ?: errStr ?: "Gagal kirim review (no error body)"
                        Toast.makeText(requireContext(), serverMsg, Toast.LENGTH_LONG).show()
                        return@launch
                    }
                }

                // Setelah review sukses -> update status completed
                markOrderCompletedToServer(orderId) {
                    CartManager.orders.find { it.id == orderId }?.apply {
                        status = "completed"
                        hasReview = true
                    }

                    Toast.makeText(requireContext(), "✅ Review masuk & status jadi completed", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                }

            } catch (e: Exception) {
                Log.e("REVIEW_ADD", "EXCEPTION", e)
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

