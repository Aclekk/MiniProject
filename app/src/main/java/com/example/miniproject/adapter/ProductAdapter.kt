package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.databinding.ItemProductGridBinding
import com.example.miniproject.model.Product

class ProductAdapter(
    private val products: MutableList<Product>,
    private val userRole: String,
    private val onItemClick: (Product, String) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductGridBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    // ✅ Fungsi tambahan untuk update list saat search / filter
    fun updateList(newList: List<Product>) {
        products.clear()
        products.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(private val binding: ItemProductGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            // Nama produk
            binding.tvProductName.text = product.name

            // Harga
            binding.tvProductPrice.text = "Rp ${String.format("%,d", product.price.toInt())}"



            // Load gambar
            when {
                product.imageResId != null -> {
                    binding.imgProduct.setImageResource(product.imageResId)
                }
                !product.imageUrl.isNullOrEmpty() -> {
                    Glide.with(binding.root.context)
                        .load(product.imageUrl)
                        .placeholder(R.drawable.bg_card)
                        .error(R.drawable.bg_card)
                        .into(binding.imgProduct)
                }
                else -> {
                    binding.imgProduct.setImageResource(R.drawable.bg_card)
                }
            }

            // Klik item
            binding.root.setOnClickListener {
                onItemClick(product, "view")
            }
        }
    }
}
