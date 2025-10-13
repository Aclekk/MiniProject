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
import com.example.miniproject.databinding.FragmentProductDetailBinding
import com.example.miniproject.model.Product
import com.example.miniproject.model.Review
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
            setupBuyNowButton() // ✅ Tambahan tombol Beli Sekarang
        }
    }

    private fun setupProductDetail(product: Product) {
        // Nama produk
        binding.tvProductName.text = product.name

        // Kategori
        binding.tvProductCategory.text = product.categoryName ?: "Tanpa Kategori"

        // Harga
        binding.tvProductPrice.text = "Rp ${String.format("%,d", product.price.toInt())}"

        // Stock
        binding.tvProductStock.text = "Stok tersedia: ${product.stock}"

        // Deskripsi
        binding.tvProductDescription.text =
            product.description ?: "Tidak ada deskripsi untuk produk ini"

        // Gambar produk
        when {
            product.imageResId != null -> {
                binding.ivProductImage.setImageResource(product.imageResId)
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

        // Setup reviews
        setupReviews()

        // Tombol kembali
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupReviews() {
        // Dummy reviews
        val dummyReviews = listOf(
            Review(1, 101, "Budi Santoso", 5f, "Produk sangat bagus, kualitas terjamin dan pengiriman cepat!", "2025-01-10"),
            Review(2, 102, "Siti Nurhaliza", 4.5f, "Cukup puas, hanya pengemasan kurang rapi", "2025-01-09"),
            Review(3, 103, "Ahmad Gunawan", 5f, "Seller responsif, produk bagus, recommended!", "2025-01-08"),
            Review(4, 104, "Eka Putri", 4f, "Produk sesuai deskripsi, tapi agak mahal", "2025-01-07"),
            Review(5, 105, "Rendra Wijaya", 5f, "Terbaik! Sudah beli 3x dan selalu puas. Worth it!", "2025-01-06")
        )

        val avgRating = dummyReviews.map { it.rating }.average().toFloat()
        binding.rbRating.rating = avgRating
        binding.tvReviewCount.text = "(${dummyReviews.size} review)"

        val reviewAdapter = ReviewAdapter(dummyReviews)
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = reviewAdapter
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
        // 🛒 Tombol Tambah ke Keranjang (bisa di layout baru, atau sementara gunakan ImageView)
        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }
    }

    private fun setupBuyNowButton() {
        // 💸 Tombol Beli Sekarang
        binding.btnBuyNow.setOnClickListener {
            currentProduct?.let { product ->
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                intent.putExtra("product", product)
                intent.putExtra("quantity", quantity)
                startActivity(intent)
            }
        }
    }

    private fun addToCart() {
        currentProduct?.let {
            val message = "Menambahkan ${quantity}x ${it.name} ke keranjang..."
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            currentProduct?.let { product ->
                repeat(quantity) {
                    CartManager.addToCart(product)
                }
            }


            // Bisa navigate ke CartFragment nanti
            // parentFragmentManager.beginTransaction()
            //     .replace(R.id.fragment_container, CartFragment())
            //     .addToBackStack(null)
            //     .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
