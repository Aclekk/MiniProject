package com.example.miniproject.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.ItemProductGridBinding
import com.example.miniproject.model.Product

class ProductAdapter(
    private val products: MutableList<Product>,
    private val userRole: String,
    private val onItemClick: (Product, String) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    init {
        Log.d("ProductAdapter", "Adapter created with userRole: '$userRole'")
        Log.d("ProductAdapter", "Initial products size: ${products.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        Log.d("ProductAdapter", "🔗 onBindViewHolder - position: $position, product: ${products[position].name}")
        holder.bind(products[position])
    }

    override fun getItemCount(): Int {
        Log.d("ProductAdapter", "📊 getItemCount() called, returning: ${products.size}")
        return products.size
    }

    fun updateList(newList: List<Product>) {
        products.clear()
        products.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(private val binding: ItemProductGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            Log.d("ProductAdapter", "============================================")
            Log.d("ProductAdapter", "Binding product: ${product.name}")
            Log.d("ProductAdapter", "Current userRole: '$userRole'")
            Log.d("ProductAdapter", "📸 imageUrl from DB: ${product.imageUrl}")

            // Set teks produk
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "Rp ${String.format("%,d", product.price.toInt())}"

            // ✅ Load foto produk dengan error handling yang lebih baik
            val imageUrl = ApiClient.getImageUrl(product.imageUrl)
            Log.d("ProductAdapter", "🌐 Full image URL: $imageUrl")

            if (imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_card)
                    .error(R.drawable.bg_card)
                    .into(binding.imgProduct)
                Log.d("ProductAdapter", "✅ Glide loading: $imageUrl")
            } else {
                binding.imgProduct.setImageResource(R.drawable.bg_card)
                Log.d("ProductAdapter", "⚠️ No image URL, using placeholder")
            }

            // Kategori dan Stok
            binding.tvProductCategory.text = product.categoryName ?: "Kategori"
            binding.tvProductStock.text = "Stok: ${product.stock}"

            // ✅ TOGGLE BEST SELLER ICON (untuk seller)
            if (userRole == "seller") {
                binding.llAdminActions.visibility = View.VISIBLE

                // ✅ Tampilkan icon best seller
                binding.btnBestSeller.visibility = View.VISIBLE

                // Set icon based on status
                if (product.isBestSeller == 1) {
                    binding.btnBestSeller.setImageResource(R.drawable.ic_star_filled) // ⭐
                } else {
                    binding.btnBestSeller.setImageResource(R.drawable.ic_star_outline) // ☆
                }

                Log.d("ProductAdapter", "✅ Seller actions VISIBLE for: ${product.name}")
            } else {
                binding.llAdminActions.visibility = View.GONE
                binding.btnBestSeller.visibility = View.GONE
                Log.d("ProductAdapter", "❌ Seller actions GONE for: ${product.name}")
            }

            // Event Listeners
            binding.btnEdit.setOnClickListener {
                Log.d("ProductAdapter", "✏️ Edit clicked for: ${product.name}")
                onItemClick(product, "edit")
            }

            binding.btnDelete.setOnClickListener {
                Log.d("ProductAdapter", "🗑️ Delete clicked for: ${product.name}")
                onItemClick(product, "delete")
            }

            // ✅ TOGGLE BEST SELLER
            binding.btnBestSeller.setOnClickListener {
                Log.d("ProductAdapter", "⭐ Toggle best seller for: ${product.name}")
                onItemClick(product, "toggle_best_seller")
            }

            binding.root.setOnClickListener {
                Log.d("ProductAdapter", "👀 View clicked for: ${product.name}")
                onItemClick(product, "view")
            }
        }
    }
}