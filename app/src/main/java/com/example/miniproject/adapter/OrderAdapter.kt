package com.example.miniproject.adapter

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.ItemOrderBinding

class OrderAdapter(
    private val orders: List<Order>,
    private val onNextStatus: ((Order) -> Unit)?
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.binding.root.context

        // 🔥 LOG DEBUGGING
        Log.d("OrderAdapter", "Binding order #${order.id} at position $position")
        Log.d("OrderAdapter", "Status: ${order.status}")

        holder.binding.tvOrderId.text = "Order #${order.id}"
        holder.binding.tvOrderStatus.text = order.status
        holder.binding.tvOrderTotal.text =
            "Rp ${String.format("%,d", order.totalPrice.toInt())}"

        val productNames = order.products.joinToString { it.name }
        holder.binding.tvOrderItems.text = productNames

        // 🔥 BUTTON ADMIN - PASTIKAN VISIBLE
        holder.binding.btnNextStatus.apply {
            visibility = View.VISIBLE

            text = when (order.status) {
                "Belum Bayar" -> "✅ Konfirmasi Pembayaran"
                "Dikemas" -> "🚚 Tandai Dikirim"
                "Dikirim" -> "✔️ Tandai Selesai"
                "Selesai" -> "✅ Selesai"
                else -> "Update"
            }

            isEnabled = order.status != "Selesai"

            // 🔥 LOG BUTTON
            Log.d("OrderAdapter", "Button text: $text, Visible: $visibility, Enabled: $isEnabled")

            setOnClickListener {
                if (order.status != "Selesai") {
                    Log.d("OrderAdapter", "Button clicked for order #${order.id}")
                    onNextStatus?.invoke(order)
                }
            }
        }
    }

    override fun getItemCount() = orders.size

    private fun showStatusNotification(context: Context, status: String) {
        val channelId = "order_status_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Status Pesanan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi perubahan status pesanan"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val message = when (status) {
            "Dikirim" -> "Pesananmu sedang dalam perjalanan 🚚"
            "Selesai" -> "Pesananmu telah tiba 🎉"
            else -> "Status pesanan: $status"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Status Pesanan 📦")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e("OrderAdapter", "❌ Error showing notification: ${e.message}")
        }
    }
}
