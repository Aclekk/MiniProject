package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.ItemOrderBinding
import com.example.miniproject.util.statusLabel

class OrderHistoryAdapter(
    val orders: MutableList<Order>,
    private val onOrderClick: (Order) -> Unit,
    private val isActiveTab: Boolean = true
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.binding.tvOrderId.text = "Order #${order.id}"

        // ✅ FIX: tampilkan label (Pending/Dikemas/Dikirim/Selesai)
        holder.binding.tvOrderStatus.text = statusLabel(order.status)

        holder.binding.tvOrderItems.text = "Barang: ${order.products.joinToString { it.name }}"
        holder.binding.tvOrderTotal.text = "Rp ${String.format("%,d", order.totalPrice.toInt())}"

        // list buyer: button disembunyikan, user klik card buat detail (logic kamu tetap)
        holder.binding.btnNextStatus.visibility = View.GONE

        holder.binding.root.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount() = orders.size
}
