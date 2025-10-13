package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.databinding.ItemOrderBinding
import com.example.miniproject.data.Order

class OrderAdapter(
    private val orders: List<Order>,
    private val onNextStatus: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.binding.root.context

        holder.binding.tvOrderId.text = "Order #${order.id}"
        holder.binding.tvOrderStatus.text = "Status: ${order.status}"
        holder.binding.tvOrderTotal.text = "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        val productNames = order.products.joinToString { it.name }
        holder.binding.tvOrderItems.text = "Barang: $productNames"

        holder.binding.btnNextStatus.apply {
            text = when (order.status) {
                "Belum Bayar" -> "Tandai Dikemas"
                "Dikemas" -> "Tandai Dikirim"
                "Dikirim" -> "Tandai Selesai"
                "Selesai" -> "Selesai ✅"
                else -> "Update"
            }
            isEnabled = order.status != "Selesai"
            setOnClickListener { onNextStatus(order) }
        }
    }

    override fun getItemCount() = orders.size
}
