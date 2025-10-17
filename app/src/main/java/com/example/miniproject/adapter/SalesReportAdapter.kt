package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.ItemSalesReportBinding

class SalesReportAdapter(
    private val orders: List<Order>
) : RecyclerView.Adapter<SalesReportAdapter.SalesViewHolder>() {

    inner class SalesViewHolder(val binding: ItemSalesReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val binding = ItemSalesReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SalesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val order = orders[position]

        holder.binding.tvOrderId.text = "Order #${order.id}"
        holder.binding.tvOrderDate.text = "12 Okt 2025" // Dummy date
        holder.binding.tvOrderItems.text = order.products.joinToString { it.name }
        holder.binding.tvOrderTotal.text = "Rp ${String.format("%,d", order.totalPrice.toInt())}"
        holder.binding.tvPaymentMethod.text = order.paymentMethod
    }

    override fun getItemCount() = orders.size
}