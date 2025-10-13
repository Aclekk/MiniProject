package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.ItemOrderHistoryBinding

class OrderHistoryAdapter(
    private val orders: List<Order>,
    private val onDetailClicked: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(val binding: ItemOrderHistoryBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            tvOrderId.text = "Order #${order.id}"
            tvOrderStatus.text = "Status: ${order.status}"
            tvOrderTotal.text = "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"
            btnDetail.setOnClickListener { onDetailClicked(order) }
        }
    }

    override fun getItemCount() = orders.size
}
