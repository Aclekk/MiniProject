package com.example.miniproject.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.model.Product
import java.text.NumberFormat
import java.util.*

class ProductAdapter(
    private val products: List<Product>,
    private val userRole: String,
    private val onItemClick: (Product, String) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvProductStock: TextView = itemView.findViewById(R.id.tvProductStock)
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val llAdminActions: LinearLayout = itemView.findViewById(R.id.llAdminActions)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(product: Product) {
            // Debug log supaya bisa lihat kategori dari API
            Log.d("ProductAdapter", "Product: ${product.name}, Category: ${product.categoryName}")

            // Set product data
            tvProductName.text = product.name
            tvProductCategory.text = product.categoryName ?: "No Category"
            tvProductStock.text = product.stock.toString()

            // Format price to Indonesian Rupiah
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvProductPrice.text = formatter.format(product.price)

            // Load image with Glide
            if (!product.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(ivProductImage)
            } else {
                ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Show/Hide admin actions based on user role
            if (userRole == "admin") {
                llAdminActions.visibility = View.VISIBLE

                btnEdit.setOnClickListener {
                    onItemClick(product, "edit")
                }

                btnDelete.setOnClickListener {
                    onItemClick(product, "delete")
                }
            } else {
                llAdminActions.visibility = View.GONE
            }

            // Click on whole item
            itemView.setOnClickListener {
                onItemClick(product, "view")
            }

            // Set stock color based on availability
            when {
                product.stock <= 0 -> {
                    tvProductStock.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    tvProductStock.text = "Out of Stock"
                }
                product.stock <= 10 -> {
                    tvProductStock.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                }
                else -> {
                    tvProductStock.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                }
            }
        }
    }
}
