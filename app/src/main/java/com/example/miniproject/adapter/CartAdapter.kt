package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.model.Product
import com.google.android.material.button.MaterialButton

class CartAdapter(
    private val items: MutableList<Product>,
    private val onRemove: (Product) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCartImage: ImageView = itemView.findViewById(R.id.ivCartImage)
        private val progressBarImage: ProgressBar = itemView.findViewById(R.id.progressBarImage)

        private val tvCartName: TextView = itemView.findViewById(R.id.tvCartName)
        private val tvCartWeight: TextView = itemView.findViewById(R.id.tvCartWeight)
        private val tvCartCategory: TextView = itemView.findViewById(R.id.tvCartCategory)
        private val tvCartPrice: TextView = itemView.findViewById(R.id.tvCartPrice)

        private val btnRemove: MaterialButton = itemView.findViewById(R.id.btnRemove)

        fun bind(product: Product) {
            tvCartName.text = product.name
            tvCartCategory.text = product.categoryName ?: "-"
            tvCartWeight.text = "1 kg" // Product model kamu belum ada weight, jadi aman default

            val priceInt = product.price.toInt()
            tvCartPrice.text = "Rp ${String.format("%,d", priceInt)}"

            // Aman: tanpa library tambahan (biar pasti compile).
            // Kalau kamu punya loader (Glide/Picasso) dan mau dipakai, bilang, nanti aku sesuaikan.
            progressBarImage.visibility = View.GONE
            val resId = product.imageResId
            if (resId != null) {
                ivCartImage.setImageResource(resId)
            } else {
                ivCartImage.setImageResource(R.drawable.bg_card)
            }

            btnRemove.setOnClickListener {
                onRemove(product)
            }
        }
    }
}
