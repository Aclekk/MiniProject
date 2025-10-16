package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.databinding.ItemCartBinding
import com.example.miniproject.model.Product
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private val cartItems: MutableList<Product>,
    private val onQuantityChanged: () -> Unit,
    private val onItemRemoved: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = currencyFormat.format(product.price)
            binding.tvQuantity.text = product.quantity.toString()

            // Set image placeholder (Anda bisa pakai Glide/Picasso untuk load dari URL)
            binding.ivProductImage.setImageResource(R.drawable.ic_product_placeholder)

            // Button increase quantity
            binding.btnPlus.setOnClickListener {
                product.quantity++
                binding.tvQuantity.text = product.quantity.toString()
                onQuantityChanged()
            }

            // Button decrease quantity
            binding.btnMinus.setOnClickListener {
                if (product.quantity > 1) {
                    product.quantity--
                    binding.tvQuantity.text = product.quantity.toString()
                    onQuantityChanged()
                }
            }

            // Button remove item
            binding.btnRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    cartItems.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItems.size)
                    onItemRemoved()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount(): Int = cartItems.size
}