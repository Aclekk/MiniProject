package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.data.Order

class OrderAdapter(
    var orders: List<Order>,
    private val onNextStatus: ((Order) -> Unit)? // boleh null kalau cuma mau tampilkan list
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvTotalPayment: TextView = itemView.findViewById(R.id.tvOrderTotal)

        val btnNextStatus: Button? =
            itemView.findViewById<Button?>(R.id.btnNextStatus)
                ?: itemView.findViewById<Button?>(R.id.btnDetail)
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
        holder.tvStatus.text = order.status
        holder.tvTotalPayment.text =
            "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        if (onNextStatus != null && holder.btnNextStatus != null) {
            holder.btnNextStatus.visibility = View.VISIBLE

            val statusLower = order.status.lowercase()

            holder.btnNextStatus.text = when {
                statusLower == "belum bayar" ->
                    "Tandai Dibayar"

                statusLower == "pending" || statusLower == "menunggu konfirmasi" ->
                    "Konfirmasi Pesanan"     // 🔥 di sinilah tombol yang kamu mau

                statusLower == "dikemas" ->
                    "Kirim Pesanan"

                statusLower == "dikirim" ->
                    "Selesai"

                else ->
                    "Ubah Status"
            }

            holder.btnNextStatus.setOnClickListener {
                onNextStatus.invoke(order)
            }
        } else {
            holder.btnNextStatus?.visibility = View.GONE
        }
    }
}
