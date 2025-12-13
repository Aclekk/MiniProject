package com.example.miniproject.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.data.Order
import com.example.miniproject.util.normalizeDbStatus
import com.example.miniproject.util.statusLabel
import com.example.miniproject.util.sellerButtonLabel

class OrderAdapter(
    var orders: MutableList<Order>,
    private val role: String? = null,
    private val onActionClick: ((Order) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvTotalPayment: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val btnAction: Button? = itemView.findViewById(R.id.btnNextStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderId.text = "Order #${order.id}"
        holder.tvTotalPayment.text = "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        // ✅ Normalize status dari DB
        val dbStatus = normalizeDbStatus(order.status)

        // ✅ Set label status
        holder.tvStatus.visibility = View.VISIBLE
        holder.tvStatus.text = statusLabel(dbStatus)

        // ✅ Set warna background status
        when (dbStatus) {
            "pending" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.tvStatus.setTextColor(Color.parseColor("#F57C00"))
            }
            "processing" -> {  // ✅ FIX: processing = dikonfirmasi (hijau)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active)
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            }
            "shipped" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_shipped)
                holder.tvStatus.setTextColor(Color.parseColor("#1976D2"))
            }
            "completed" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_done)
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
            }
            "cancelled" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"))
            }
            else -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.tvStatus.setTextColor(Color.GRAY)
            }
        }

        // ✅ Show button hanya untuk seller/admin
        val canShowButton = (
                onActionClick != null &&
                        holder.btnAction != null &&
                        !role.isNullOrBlank()
                )

        if (!canShowButton) {
            holder.btnAction?.visibility = View.GONE
            return
        }

        // ✅ Logika button untuk SELLER/ADMIN
        when (role!!.lowercase()) {
            "seller", "admin" -> {
                when (dbStatus) {
                    "pending" -> {
                        holder.btnAction?.visibility = View.VISIBLE
                        holder.btnAction?.text = "📦 Konfirmasi Pesanan"
                    }
                    "processing" -> {  // ✅ FIX UTAMA: processing = show "Kirim"
                        holder.btnAction?.visibility = View.VISIBLE
                        holder.btnAction?.text = "🚚 Kirim Pesanan"
                    }
                    "shipped", "completed", "cancelled" -> {
                        holder.btnAction?.visibility = View.GONE
                    }
                    else -> {
                        holder.btnAction?.visibility = View.GONE
                    }
                }
            }
            else -> {
                holder.btnAction?.visibility = View.GONE
            }
        }

        holder.btnAction?.setOnClickListener {
            onActionClick?.invoke(order)
        }
    }

    fun replaceAll(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}