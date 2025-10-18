package com.example.miniproject.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.databinding.ItemProductGridBinding
import com.example.miniproject.model.Product

class ProductAdapter(
    private val products: MutableList<Product>,  // ← Ini akan jadi shared reference
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

    // ❌ HAPUS METHOD INI - TIDAK PERLU LAGI!
    // fun updateList(newList: List<Product>) {
    //     products.clear()
    //     products.addAll(newList)
    //     notifyDataSetChanged()
    // }

    inner class ProductViewHolder(private val binding: ItemProductGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            Log.d("ProductAdapter", "============================================")
            Log.d("ProductAdapter", "Binding product: ${product.name}")
            Log.d("ProductAdapter", "Current userRole: '$userRole'")

            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "Rp ${String.format("%,d", product.price.toInt())}"

            // ✅ FIXED: Smart cast error — pakai null-safe
            when {
                product.imageResId != null -> {
                    binding.imgProduct.setImageResource(product.imageResId ?: R.drawable.bg_card)
                    Log.d("ProductAdapter", "🖼️ Using local resource image for ${product.name}")
                }
                !product.imageUrl.isNullOrEmpty() -> {
                    Glide.with(binding.root.context)
                        .load(product.imageUrl)
                        .placeholder(R.drawable.bg_card)
                        .into(binding.imgProduct)
                    Log.d("ProductAdapter", "🌐 Using URL image for ${product.name}")
                }
                else -> {
                    binding.imgProduct.setImageResource(R.drawable.bg_card)
                    Log.d("ProductAdapter", "📦 Using default image for ${product.name}")
                }
            }

            // 🔹 Set kategori & stok (jika ada di layout)
            binding.tvProductCategory.text = product.categoryName ?: "Kategori"
            binding.tvProductStock.text = "Stok: ${product.stock}"

            // ✅ Hanya tampilkan tombol edit/delete untuk admin
            if (userRole == "admin") {
                binding.llAdminActions.visibility = View.VISIBLE
                Log.d("ProductAdapter", "✅ Admin actions VISIBLE for: ${product.name}")
            } else {
                binding.llAdminActions.visibility = View.GONE
                Log.d("ProductAdapter", "❌ Admin actions GONE for: ${product.name}, role: '$userRole'")
            }

            // 🟡 Edit Produk
            binding.btnEdit.setOnClickListener {
                Log.d("ProductAdapter", "✏️ Edit clicked for: ${product.name}")
                onItemClick(product, "edit")
            }

            // 🔴 Hapus Produk
            binding.btnDelete.setOnClickListener {
                Log.d("ProductAdapter", "🗑️ Delete clicked for: ${product.name}")
                onItemClick(product, "delete")
            }

            // 👁️ Klik card produk (detail)
            binding.root.setOnClickListener {
                Log.d("ProductAdapter", "👀 View clicked for: ${product.name}")
                onItemClick(product, "view")
            }
        }
    }
}