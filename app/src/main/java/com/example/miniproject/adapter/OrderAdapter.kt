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
        // ID disesuaikan dengan item_order_history.xml
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)

        // ⚠️ DI SINI YANG TADI BIKIN ERROR:
        // xml: tvOrderStatus & tvOrderTotal, jadi kita mapping ke sana
        val tvStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvTotalPayment: TextView = itemView.findViewById(R.id.tvOrderTotal)

        // Gunakan btnNextStatus kalau ada, kalau tidak pakai btnDetail yang lama
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

        // Tombol ubah status dipakai di AdminOrderListFragment.
        // Sekarang belum ke database, cuma ubah status di memori.
        if (onNextStatus != null && holder.btnNextStatus != null) {
            holder.btnNextStatus.visibility = View.VISIBLE

            holder.btnNextStatus.text = when (order.status) {
                "Belum Bayar" -> "Proses"
                "Dikemas"     -> "Kirim Pesanan"
                "Dikirim"     -> "Selesai"
                else          -> "Ubah Status"
            }

            holder.btnNextStatus.setOnClickListener {
                onNextStatus.invoke(order)
            }
        } else {
            // Kalau adapter dipakai tanpa aksi next status → sembunyikan
            holder.btnNextStatus?.visibility = View.GONE
        }
    }
}
