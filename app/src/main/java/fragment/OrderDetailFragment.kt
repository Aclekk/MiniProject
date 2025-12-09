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
            "- ${it.name} (Rp ${String.format("%,d", it.price.toInt())})"
        }
        binding.tvOrderProducts.text = "Produk:\n$productList"
        binding.tvOrderTotal.text = "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        // 🔥 UPDATE BUTTON VISIBILITY
        updateButtonVisibility(order)

        // 🔹 Button "Pesanan Telah Sampai" (User confirm delivery)
        binding.btnNextStatus.setOnClickListener {
            when (order.status) {
                "Dikirim" -> {
                    order.status = "Selesai"
                    binding.tvOrderStatus.text = "Status: ${order.status}"
                    updateButtonVisibility(order)

                    Toast.makeText(
                        requireContext(),
                        "Pesanan selesai! Berikan penilaian Anda 🌟",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    Toast.makeText(
                        requireContext(),
                        "Tidak ada aksi untuk status ini",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // 🔹 Button "Beri Penilaian"
        binding.btnRateNow.setOnClickListener {
            showRatingDialog(order)
        }
    }

    // 🔥 LOGIC TAMPILKAN BUTTON SESUAI STATUS
    private fun updateButtonVisibility(order: com.example.miniproject.data.Order) {
        when {
            // Status "Dikirim" → Tampilkan button "Selesai" (user menyelesaikan pesanan)
            order.status == "Dikirim" -> {
                binding.btnNextStatus.visibility = View.VISIBLE
                binding.btnNextStatus.text = "Selesai"           // ⬅️ dulu: "✅ Pesanan Telah Sampai"
                binding.btnRateNow.visibility = View.GONE
            }

            // Status "Selesai" & belum review → Tampilkan button "Beri Penilaian"
            order.status == "Selesai" && !order.hasReview -> {
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.VISIBLE
                binding.btnRateNow.text = "⭐ Beri Penilaian"
            }

            // Sudah review → Sembunyikan semua button
            order.hasReview -> {
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.GONE
            }

            // Status lain (Dikemas, Belum Bayar) → Sembunyikan button
            else -> {
                binding.btnNextStatus.visibility = View.GONE
                binding.btnRateNow.visibility = View.GONE
            }
        }
    }


    // 🌟 Dialog Rating & Review
    private fun showRatingDialog(order: com.example.miniproject.data.Order) {
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
                    Toast.makeText(
                        requireContext(),
                        "Berikan rating minimal 1 bintang ⭐",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                // Simpan review untuk setiap produk
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

                // Tandai order sudah direview
                order.hasReview = true
                updateButtonVisibility(order)

                Toast.makeText(
                    requireContext(),
                    "Terima kasih atas penilaian Anda! ⭐",
                    Toast.LENGTH_LONG
                ).show()

                // Kembali ke list
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