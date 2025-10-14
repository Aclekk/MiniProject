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

        // Tombol "Beri Penilaian" hanya muncul kalau status Selesai
        binding.btnRateNow.visibility =
            if (order.status == "Selesai") View.VISIBLE else View.GONE

        binding.btnRateNow.setOnClickListener {
            showRatingDialog(order.products.first().id)
        }

        // ✅ Tambahkan tombol "Ubah Status"
        binding.btnNextStatus.setOnClickListener {
            val currentStatus = order.status
            val nextStatus = when (currentStatus) {
                "Belum Bayar" -> "Dikemas"
                "Dikemas" -> "Dikirim"
                "Dikirim" -> "Selesai"
                else -> "Selesai"
            }

            order.status = nextStatus
            binding.tvOrderStatus.text = "Status: ${order.status}"

            Toast.makeText(
                requireContext(),
                "Status diubah ke ${order.status}",
                Toast.LENGTH_SHORT
            ).show()

            // Kalau sudah selesai, munculkan tombol rating
            if (order.status == "Selesai") {
                binding.btnRateNow.visibility = View.VISIBLE
            }
        }
    }

    // 🔸 Dialog Rating
    private fun showRatingDialog(productId: Int) {
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
                    productId = productId,
                    userName = "Rachen 🌾",
                    rating = rating,
                    comment = comment,
                    createdAt = "2025-10-14"
                )

                CartManager.addReview(review)
                Toast.makeText(
                    requireContext(),
                    "Terima kasih atas penilaiannya!",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
