package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderUserAdapter(
    private val orders: List<Order>,
    private val onOrderCompleted: (Order) -> Unit
) : RecyclerView.Adapter<OrderUserAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardOrder: CardView = itemView.findViewById(R.id.cardOrder)
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvStatusIcon: TextView = itemView.findViewById(R.id.tvStatusIcon)
        val tvStatusDescription: TextView = itemView.findViewById(R.id.tvStatusDescription)
        val btnOrderArrived: Button = itemView.findViewById(R.id.btnOrderArrived)

        fun bind(order: Order) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            tvOrderId.text = "Order #${order.id}"
            tvOrderDate.text = "📅 ${dateFormat.format(order.orderDate)}"
            tvOrderItems.text = order.items.joinToString("\n")
            tvOrderTotal.text = "Total: ${currencyFormat.format(order.totalPrice)}"
            tvOrderStatus.text = order.status

            // Warna, icon, dan deskripsi berdasarkan status
            when (order.status) {
                "Menunggu Konfirmasi" -> {
                    tvOrderStatus.setTextColor(0xFFFF9800.toInt())
                    tvStatusIcon.text = "⏳"
                    tvStatusDescription.text = "Pesanan Anda sedang menunggu konfirmasi dari penjual"
                    btnOrderArrived.visibility = View.GONE
                    cardOrder.setCardBackgroundColor(0xFFFFF3E0.toInt())
                }
                "Dikemas" -> {
                    tvOrderStatus.setTextColor(0xFF2196F3.toInt())
                    tvStatusIcon.text = "📦"
                    tvStatusDescription.text = "Pesanan Anda sedang dikemas oleh penjual"
                    btnOrderArrived.visibility = View.GONE
                    cardOrder.setCardBackgroundColor(0xFFE3F2FD.toInt())
                }
                "Dikirim" -> {
                    tvOrderStatus.setTextColor(0xFF9C27B0.toInt())
                    tvStatusIcon.text = "🚚"
                    tvStatusDescription.text = "Pesanan Anda sedang dalam pengiriman"
                    // TAMPILKAN TOMBOL "PESANAN TIBA" ketika status = Dikirim
                    btnOrderArrived.visibility = View.VISIBLE
                    btnOrderArrived.text = "✅ Pesanan Tiba"
                    btnOrderArrived.setOnClickListener {
                        onOrderCompleted(order)
                    }
                    cardOrder.setCardBackgroundColor(0xFFF3E5F5.toInt())
                }
                "Selesai" -> {
                    tvOrderStatus.setTextColor(0xFF4CAF50.toInt())
                    tvStatusIcon.text = "✅"
                    tvStatusDescription.text = "Pesanan telah selesai. Terima kasih!"
                    btnOrderArrived.visibility = View.VISIBLE
                    btnOrderArrived.text = "✅ Selesai"
                    btnOrderArrived.isEnabled = false
                    btnOrderArrived.alpha = 0.5f
                    cardOrder.setCardBackgroundColor(0xFFE8F5E9.toInt())
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_user, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount() = orders.size
}