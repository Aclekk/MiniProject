package com.example.miniproject.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.adapter.ReviewAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.ProductDataSource
import com.example.miniproject.databinding.FragmentProductDetailBinding
import com.example.miniproject.model.Product
import com.example.miniproject.ui.CheckoutActivity

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private var quantity = 1
    private var currentProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = arguments?.getParcelable<Product>("product")
        if (product != null) {
            currentProduct = product
            setupProductDetail(product)
            setupQuantityButtons()
            setupAddToCartButton()
            setupBuyNowButton()
            setupReviews(product.id) // ✅ Aktifkan ulasan di sini
        }
    }

    private fun setupProductDetail(product: Product) {
        binding.tvProductName.text = product.name
        binding.tvProductCategory.text = product.categoryName ?: "Tanpa Kategori"
        binding.tvProductPrice.text = "Rp ${String.format("%,d", product.price.toInt())}"
        binding.tvProductStock.text = "Stok tersedia: ${product.stock}"
        binding.tvProductDescription.text = product.description ?: "Tidak ada deskripsi untuk produk ini"

        when {
            product.imageResId != null -> {
                binding.ivProductImage.setImageResource(product.imageResId ?: R.drawable.bg_card)
            }
            !product.imageUrl.isNullOrEmpty() -> {
                Glide.with(requireContext())
                    .load(product.imageUrl)
                    .placeholder(R.drawable.bg_card)
                    .error(R.drawable.bg_card)
                    .into(binding.ivProductImage)
            }
            else -> {
                binding.ivProductImage.setImageResource(R.drawable.bg_card)
            }
        }

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun setupReviews(productId: Int) {
        val allReviews = CartManager.reviews.filter { it.productId == productId }

        if (allReviews.isEmpty()) {
            binding.rvReviews.visibility = View.GONE
            binding.tvReviewCount.text = "(Belum ada ulasan)"
            return
        }

        binding.rvReviews.visibility = View.VISIBLE

        val product = ProductDataSource.getAllProducts().find { it.id == productId }

        // 🔗 Buat Triple agar cocok dengan ReviewAdapter
        val reviewTriples = allReviews.map {
            Triple(it, product?.name ?: "Produk Tidak Dikenal", product?.imageResId)
        }

        val adapter = ReviewAdapter(reviewTriples)
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter

        val avgRating = allReviews.map { it.rating }.average().toFloat()
        binding.rbRating.rating = avgRating
        binding.tvReviewCount.text = "(${allReviews.size} ulasan)"
    }

    private fun setupQuantityButtons() {
        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }

        binding.btnPlus.setOnClickListener {
            if (currentProduct != null && quantity < currentProduct!!.stock) {
                quantity++
                binding.tvQuantity.text = quantity.toString()
            } else {
                Toast.makeText(requireContext(), "Stok tidak mencukupi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAddToCartButton() {
        binding.btnAddToCart.setOnClickListener {
            currentProduct?.let { product ->
                repeat(quantity) { CartManager.addToCart(product) }
                Toast.makeText(
                    requireContext(),
                    "Menambahkan ${quantity}x ${product.name} ke keranjang...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupBuyNowButton() {
        binding.btnBuyNow.setOnClickListener {
            currentProduct?.let { product ->
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                intent.putExtra("product", product)
                intent.putExtra("quantity", quantity)
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
