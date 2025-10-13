package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.databinding.ItemCartBinding
import com.example.miniproject.model.Product

class CartAdapter(
    private val cartItems: List<Product>,
    private val onRemove: (Product) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = cartItems[position]
        with(holder.binding) {
            tvCartName.text = product.name
            tvCartPrice.text = "Rp ${String.format("%,d", product.price.toInt())}"

            Glide.with(root.context)
                .load(product.imageResId ?: R.drawable.bg_card)
                .into(ivCartImage)

            btnRemove.setOnClickListener { onRemove(product) }
        }
    }

    override fun getItemCount() = cartItems.size
}
