package com.example.miniproject.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Review
import com.example.miniproject.databinding.FragmentOrderDetailBinding

class OrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderId = arguments?.getInt("orderId") ?: 0
        if (orderId == 0) {
            Toast.makeText(requireContext(), "Order ID tidak valid", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        val order = CartManager.orders.find { it.id == orderId }
        if (order == null) {
            Toast.makeText(requireContext(), "Order tidak ditemukan di lokal", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

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

        updateButtonVisibility(order)

        // tombol utama buyer: selesai / kirim ulasan
        binding.btnNextStatus.setOnClickListener {
            when (order.status.lowercase()) {
                "dikirim", "shipped" -> {
                    order.status = "Selesai"
                    binding.tvOrderStatus.text = "Status: ${order.status}"
                    updateButtonVisibility(order)
                    Toast.makeText(requireContext(), "Pesanan selesai! Berikan ulasan ⭐", Toast.LENGTH_SHORT).show()
                }

                "selesai", "completed" -> {
                    if (!order.hasReview) showRatingDialog(order.id, order.products)
                }

                else -> {
                    Toast.makeText(requireContext(), "Aksi tidak tersedia untuk status ini", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnRateNow.setOnClickListener {
            if (!order.hasReview) showRatingDialog(order.id, order.products)
        }
    }

    private fun updateButtonVisibility(order: com.example.miniproject.data.Order) {
        val st = order.status.lowercase()

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

    private fun showRatingDialog(orderId: Int, products: List<com.example.miniproject.model.Product>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etComment)

        AlertDialog.Builder(requireContext())
            .setTitle("Beri Penilaian untuk Order #$orderId")
            .setView(dialogView)
            .setPositiveButton("Kirim") { _, _ ->
                val rating = ratingBar.rating
                val comment = etComment.text.toString()

                if (rating <= 0f) {
                    Toast.makeText(requireContext(), "Rating minimal 1 bintang ⭐", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // simpan review lokal (buyer device)
                products.forEach { product ->
                    CartManager.addReview(
                        Review(
                            orderId = orderId,
                            productId = product.id,
                            productName = product.name,
                            productImageUrl = product.imageUrl,
                            rating = rating,
                            comment = comment.ifEmpty { "Tidak ada komentar" },
                            userName = "Buyer"
                        )
                    )
                }

                // tandai order sudah direview (lokal)
                CartManager.orders.find { it.id == orderId }?.hasReview = true

                Toast.makeText(requireContext(), "Ulasan tersimpan (lokal) ⭐", Toast.LENGTH_LONG).show()
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
