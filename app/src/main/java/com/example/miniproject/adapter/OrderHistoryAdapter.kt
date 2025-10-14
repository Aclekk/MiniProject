package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.ItemOrderBinding

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
        holder.binding.tvOrderStatus.text = "Status: ${order.status}"
        holder.binding.tvOrderItems.text = "Barang: ${order.products.joinToString { it.name }}"
        holder.binding.tvOrderTotal.text =
            "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        // hanya tampilkan tombol ubah status di tab aktif
        if (isActiveTab && order.status != "Selesai") {
            holder.binding.btnNextStatus.visibility = View.VISIBLE
            holder.binding.btnNextStatus.text = when (order.status) {
                "Belum Bayar" -> "Tandai Dikemas"
                "Dikemas" -> "Tandai Dikirim"
                "Dikirim" -> "Tandai Selesai"
                else -> "Update"
            }
            holder.binding.btnNextStatus.setOnClickListener { onOrderClick(order) }
        } else {
            holder.binding.btnNextStatus.visibility = View.GONE
        }

        // klik card untuk lihat detail
        holder.binding.root.setOnClickListener { onOrderClick(order) }
    }

    override fun getItemCount() = orders.size
}
