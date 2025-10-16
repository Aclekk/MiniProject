package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.databinding.ItemOrderManagementBinding
import com.example.miniproject.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderManagementAdapter(
    private val orders: MutableList<Order>,
    private val onStatusChangeClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderManagementAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemOrderManagementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

            binding.tvOrderId.text = "Order #${order.id}"
            binding.tvUserName.text = "Pembeli: ${order.userName}"
            binding.tvOrderDate.text = dateFormat.format(Date(order.orderDate))
            binding.tvOrderStatus.text = "Status: ${order.status}"
            binding.tvTotalPrice.text = currencyFormat.format(order.totalPrice)

            // Tampilkan produk
            val productList = order.products.joinToString("\n") { "- ${it.name} (${it.quantity}x)" }
            binding.tvProductList.text = productList

            // Tombol ubah status
            when (order.status) {
                "Menunggu Konfirmasi" -> {
                    binding.btnChangeStatus.text = "Kemas Pesanan 📦"
                    binding.btnChangeStatus.visibility = View.VISIBLE
                }
                "Dikemas" -> {
                    binding.btnChangeStatus.text = "Kirim Pesanan 🚚"
                    binding.btnChangeStatus.visibility = View.VISIBLE
                }
                "Dikirim" -> {
                    binding.btnChangeStatus.text = "Selesaikan ✅"
                    binding.btnChangeStatus.visibility = View.VISIBLE
                }
                "Selesai" -> {
                    binding.btnChangeStatus.visibility = View.GONE
                }
            }

            binding.btnChangeStatus.setOnClickListener {
                onStatusChangeClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderManagementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun updateData(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}