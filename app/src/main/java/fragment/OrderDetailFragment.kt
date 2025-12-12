package com.example.miniproject.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
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
import com.example.miniproject.data.Order
import com.example.miniproject.data.Review
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentOrderDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private var currentOrder: Order? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderId = arguments?.getInt("orderId") ?: return
        val order = CartManager.orders.find { it.id == orderId } ?: return
        currentOrder = order

        // TAMPILKAN DETAIL PESANAN (LOGIC LAMA)
        binding.tvOrderId.text = "Order #${order.id}"
        binding.tvOrderStatus.text = "Status: ${order.status}"
        binding.tvOrderPayment.text = "Metode Pembayaran: ${order.paymentMethod}"
        binding.tvOrderAddress.text = "Alamat: ${order.address}"

        val productList = order.products.joinToString("\n") {
            "- ${it.name} (Rp ${String.format("%,d", it.price.toInt())})"
        }
        binding.tvOrderProducts.text = "Produk:\n$productList"
        binding.tvOrderTotal.text = "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        // 🔥 Atur tombol sesuai status
        updateButtonVisibility(order)

        // 🔹 Button buyer: "Pesanan Telah Sampai" / "Selesai"
        binding.btnNextStatus.setOnClickListener {
            if (order.status == "Dikirim") {
                binding.btnNextStatus.isEnabled = false

                // ✅ kirim slug backend: completed (bukan "Selesai")
                updateStatusToServer(order.id, "completed") {
                    // ✅ kalau server sukses, baru update lokal/UI
                    order.status = "Selesai"
                    binding.tvOrderStatus.text = "Status: ${order.status}"
                    updateButtonVisibility(order)

                    Toast.makeText(requireContext(), "Pesanan selesai ✅", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            } else {
                Toast.makeText(requireContext(), "Tidak ada aksi untuk status ini", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔹 Button "Beri Penilaian"
        binding.btnRateNow.setOnClickListener {
            showRatingDialog(order)
        }
    }

    /**
     * ✅ Versi merge: update status ke server (IO thread), callback sukses di Main
     * newStatus = slug backend: processing/packed/shipped/completed/...
     */
    private fun updateStatusToServer(orderId: Int, newStatus: String, onSuccess: () -> Unit) {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token login tidak ditemukan. Coba login ulang.", Toast.LENGTH_LONG).show()
            binding.btnNextStatus.isEnabled = true
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val body = mapOf(
                    "order_id" to orderId.toString(),
                    "status" to newStatus
                )

                val resp = ApiClient.apiService.updateOrderStatus(
                    token = "Bearer $token",
                    body = body
                )

                withContext(Dispatchers.Main) {
                    if (resp.isSuccessful && resp.body()?.success == true) {
                        onSuccess()
                    } else {
                        val err = resp.errorBody()?.string()
                        Toast.makeText(
                            requireContext(),
                            "Gagal update status (HTTP ${resp.code()})\n${err ?: (resp.body()?.message ?: "")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.btnNextStatus.isEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    binding.btnNextStatus.isEnabled = true
                }
            }
        }
    }

    // 🔥 LOGIC TAMPILKAN BUTTON SESUAI STATUS (PUNYA KAMU, TETAP)
    private fun updateButtonVisibility(order: Order) {
        when {
            order.status == "Dikirim" -> {
                binding.btnNextStatus.visibility = View.VISIBLE
                binding.btnNextStatus.text = "Pesanan Telah Selesai"
                binding.btnRateNow.visibility = View.GONE
            }

            order.status == "Selesai" && !order.hasReview -> {
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

    // 🌟 Dialog Rating & Review (LOGIC ASLI, TIDAK DIUTAK-ATIK)
    private fun showRatingDialog(order: Order) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etComment)

        AlertDialog.Builder(requireContext())
            .setTitle("Beri Penilaian untuk Order #${order.id}")
            .setView(dialogView)
            .setPositiveButton("Kirim") { _, _ ->
                val rating = ratingBar.rating
                val comment = etComment.text.toString()

                if (rating == 0f) {
                    Toast.makeText(requireContext(), "Berikan rating minimal 1 bintang ⭐", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                order.products.forEach { product ->
                    val review = Review(
                        orderId = order.id,
                        productId = product.id,
                        userName = "Rachen 🌾",
                        rating = rating,
                        comment = comment.ifEmpty { "Tidak ada komentar" }
                    )
                    CartManager.addReview(review)
                }

                order.hasReview = true
                updateButtonVisibility(order)

                Toast.makeText(requireContext(), "Terima kasih atas penilaian Anda! ⭐", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
