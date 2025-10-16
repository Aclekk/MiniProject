package com.example.miniproject.fragment

import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Review
import com.example.miniproject.databinding.FragmentOrderDetailBinding

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

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

        // Tampilkan detail pesanan
        binding.tvOrderId.text = "Order #${order.id}"
        binding.tvOrderStatus.text = "Status: ${order.status}"
        binding.tvOrderPayment.text = "Metode Pembayaran: ${order.paymentMethod}"
        binding.tvOrderAddress.text = "Alamat: ${order.address}"

        val productList = order.products.joinToString("\n") {
            "- ${it.name} (Rp ${it.price.toInt()})"
        }
        binding.tvOrderProducts.text = productList
        binding.tvOrderTotal.text = "Total: Rp ${order.totalPrice.toInt()}"

        // 🆕 FIX: Tombol hanya muncul sesuai status
        updateButtonVisibility(order.status)

        // 🔹 Tombol "Ubah Status"
        binding.btnNextStatus.setOnClickListener {
            val currentStatus = order.status
            val nextStatus = when (currentStatus) {
                "Dikemas" -> "Dikirim"
                "Dikirim" -> "Diterima" // 🆕 Ubah ke "Diterima" dulu
                "Diterima" -> "Selesai" // 🆕 Baru "Selesai" setelah review
                else -> currentStatus
            }

            order.status = nextStatus
            binding.tvOrderStatus.text = "Status: ${order.status}"

            Toast.makeText(
                requireContext(),
                "Status diubah ke ${order.status}",
                Toast.LENGTH_SHORT
            ).show()

            // Update tampilan tombol
            updateButtonVisibility(order.status)

            // 🆕 FIX: Refresh list di ActiveOrdersFragment & CompletedOrdersFragment
            refreshOrderLists()
        }

        // 🔹 Tombol "Beri Penilaian"
        binding.btnRateNow.setOnClickListener {
            showRatingDialog(order)
        }
    }

    // 🆕 FIX: Atur visibilitas tombol berdasarkan status
    private fun updateButtonVisibility(status: String) {
        when (status) {
            "Dikemas", "Dikirim" -> {
                // Tampilkan tombol "Ubah Status" saja
                binding.btnNextStatus.visibility = View.VISIBLE
                binding.btnRateNow.visibility = View.GONE
            }
            "Diterima" -> {
                // Tampilkan tombol "Beri Penilaian" saja
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.VISIBLE
            }
            "Selesai" -> {
                // Sembunyikan semua tombol
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.GONE
            }
            else -> {
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.GONE
            }
        }
    }

    // 🆕 FIX: Refresh list setelah ubah status
    private fun refreshOrderLists() {
        // Cari fragment ActiveOrdersFragment & CompletedOrdersFragment
        val cartFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.fragment_container) as? CartFragment

        // Trigger refresh di kedua tab
        cartFragment?.let {
            // Fragment akan auto-refresh saat onResume() dipanggil
            parentFragmentManager.popBackStack()
        }
    }

    // 🔹 Dialog Rating
    private fun showRatingDialog(order: com.example.miniproject.data.Order) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etReview = dialogView.findViewById<EditText>(R.id.etReview)

        AlertDialog.Builder(requireContext())
            .setTitle("Beri Penilaian")
            .setView(dialogView)
            .setPositiveButton("Kirim") { dialog, _ ->
                val rating = ratingBar.rating
                val comment = etReview.text.toString()

                if (rating == 0f || comment.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Isi rating & ulasan terlebih dahulu 🌾",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val review = Review(
                    productId = order.products.first().id,
                    userName = "Rachen 🌾",
                    rating = rating,
                    comment = comment,
                    createdAt = "2025-10-16"
                )

                CartManager.addReview(review)

                // 🆕 FIX: Ubah status jadi "Selesai" setelah review
                order.status = "Selesai"
                binding.tvOrderStatus.text = "Status: ${order.status}"
                updateButtonVisibility(order.status)

                Toast.makeText(
                    requireContext(),
                    "Terima kasih atas penilaiannya! Pesanan dipindah ke Riwayat.",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()

                // Refresh list dan kembali
                refreshOrderLists()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}