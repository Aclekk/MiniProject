package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.databinding.ItemOrderBinding
import com.example.miniproject.model.Order

class OrderSellerAdapter(
    private val onActionClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderSellerAdapter.ViewHolder>() {

    private val orderList = mutableListOf<Order>()

    inner class ViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvOrderId.text = "Order #${order.id}"

            // ✅ Ambil nama produk dari products list
            val productNames = order.products.joinToString(", ") { it.name }
            binding.tvUserName.text = "Produk: $productNames"

            binding.tvTotalPrice.text = "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"
            binding.tvStatus.text = "Status: ${order.status}"

            binding.btnNextStatus.text = when (order.status) {
                "Menunggu Konfirmasi" -> "Kemas Barang 📦"
                "Dikemas" -> "Kirim Barang 🚚"
                "Dikirim" -> "Selesaikan ✅"
                else -> "Selesai"
            }
            binding.btnNextStatus.isEnabled = order.status != "Selesai"
            binding.btnNextStatus.setOnClickListener { onActionClick(order) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int = orderList.size

    fun setData(newList: List<Order>) {
        orderList.clear()
        orderList.addAll(newList)
        notifyDataSetChanged()
    }
}