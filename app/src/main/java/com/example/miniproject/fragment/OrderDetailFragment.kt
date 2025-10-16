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
import com.example.miniproject.model.Review
import com.example.miniproject.databinding.FragmentOrderDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale

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

        // ✅ Terima orderId sebagai String
        val orderId = arguments?.getString("orderId") ?: return
        val order = CartManager.orders.find { it.id == orderId } ?: return

        // Format tanggal
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val formattedDate = dateFormat.format(order.orderDate)

        // Tampilkan detail pesanan
        binding.tvOrderId.text = "Order #${order.id}"
        binding.tvOrderStatus.text = "Status: ${order.status}"

        // Tampilkan info tambahan (userName dan tanggal)
        binding.tvOrderPayment.text = "Pemesan: ${order.userName}"
        binding.tvOrderAddress.text = "Tanggal: $formattedDate"

        // Tampilkan daftar produk dari items (List<String>)
        val productList = order.items?.joinToString("\n") { "- $it" } ?: "Tidak ada produk"
        binding.tvOrderProducts.text = "Produk:\n$productList"

        binding.tvOrderTotal.text = "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        // Tombol "Beri Penilaian" hanya muncul kalau status Selesai
        binding.btnRateNow.visibility =
            if (order.status == "Selesai") View.VISIBLE else View.GONE

        binding.btnRateNow.setOnClickListener {
            // Ambil productId dari produk pertama
            val productId = order.products.firstOrNull()?.id ?: "1"
            showRatingDialog(productId)
        }

        // ✅ Tombol "Ubah Status"
        binding.btnNextStatus.setOnClickListener {
            val currentStatus = order.status
            val nextStatus = when (currentStatus) {
                "Menunggu Konfirmasi" -> "Dikirim"
                "Dikirim" -> "Selesai"
                else -> "Selesai"
            }

            CartManager.updateOrderStatus(order.id, nextStatus)
            binding.tvOrderStatus.text = "Status: $nextStatus"

            Toast.makeText(
                requireContext(),
                "Status diubah ke $nextStatus",
                Toast.LENGTH_SHORT
            ).show()

            // Kalau sudah selesai, munculkan tombol rating
            if (nextStatus == "Selesai") {
                binding.btnRateNow.visibility = View.VISIBLE
            }
        }
    }

    // 🔸 Dialog Rating
    private fun showRatingDialog(productId: String) {
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
                    id = "REV${System.currentTimeMillis()}",
                    productId = productId,
                    userName = "Rachen 🌾",
                    rating = rating,
                    comment = comment,
                    reviewDate = System.currentTimeMillis()
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