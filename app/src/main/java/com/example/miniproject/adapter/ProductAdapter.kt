package com.example.miniproject.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
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
        val binding = ItemProductGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(private val binding: ItemProductGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = "Rp ${String.format("%,d", product.price.toInt())}"
            binding.tvCategory.text = product.categoryName

            // Load gambar
            when {
                product.imageResId != null -> binding.imgProduct.setImageResource(product.imageResId)
                !product.imageUrl.isNullOrEmpty() -> {
                    Glide.with(binding.root.context)
                        .load(product.imageUrl)
                        .placeholder(R.drawable.bg_card)
                        .error(R.drawable.bg_card)
                        .into(binding.imgProduct)
                }
                else -> binding.imgProduct.setImageResource(R.drawable.bg_card)
            }

            // 🟢 SHOW / HIDE tombol edit-delete sesuai role
            if (userRole == "admin") {
                binding.btnEdit.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE
            } else {
                binding.btnEdit.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
            }

            // 🖱️ Event click
            binding.btnEdit.setOnClickListener { onItemClick(product, "edit") }
            binding.btnDelete.setOnClickListener { onItemClick(product, "delete") }
            binding.root.setOnClickListener { onItemClick(product, "view") }
        }
    }
}
