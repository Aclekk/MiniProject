package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.databinding.ItemOrderHistoryBinding
import com.example.miniproject.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    val orders: MutableList<Order>,
    private val onOrderClick: (Order) -> Unit,
    private val isActiveTab: Boolean
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

            binding.tvOrderId.text = "Order #${order.id}"
            binding.tvOrderDate.text = dateFormat.format(Date(order.orderDate))
            binding.tvOrderStatus.text = order.status
            binding.tvOrderTotal.text = currencyFormat.format(order.totalPrice)

            // Tampilkan produk
            val productNames = order.products.joinToString(", ") { "${it.name} (${it.quantity}x)" }
            binding.tvOrderProducts.text = productNames

            // Set status color
            binding.tvOrderStatus.setTextColor(
                binding.root.context.getColor(
                    when (order.status) {
                        "Menunggu Konfirmasi" -> android.R.color.holo_orange_dark
                        "Dikemas" -> android.R.color.holo_blue_dark
                        "Dikirim" -> android.R.color.holo_purple
                        "Selesai" -> android.R.color.holo_green_dark
                        else -> android.R.color.darker_gray
                    }
                )
            )

            binding.root.setOnClickListener {
                onOrderClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}