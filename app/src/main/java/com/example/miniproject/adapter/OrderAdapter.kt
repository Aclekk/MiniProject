package com.example.miniproject.adapter

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
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

        holder.binding.tvOrderId.text = "Order #${order.id}"
        holder.binding.tvOrderStatus.text = "Status: ${order.status}"
        holder.binding.tvOrderTotal.text =
            "Total: Rp ${String.format("%,d", order.totalPrice.toInt())}"

        val productNames = order.products.joinToString { it.name }
        holder.binding.tvOrderItems.text = "Barang: $productNames"

        holder.binding.btnNextStatus.apply {
            text = when (order.status) {
                "Belum Bayar" -> "Tandai Dikemas"
                "Dikemas" -> "Tandai Dikirim"
                "Dikirim" -> "Tandai Selesai"
                "Selesai" -> "Selesai ✅"
                else -> "Update"
            }
            isEnabled = order.status != "Selesai"

            setOnClickListener {
                onNextStatus?.invoke(order)

                if (order.status == "Dikirim" || order.status == "Selesai") {
                    showStatusNotification(context, order.status)
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
            else -> "Status pesanan kamu: $status"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Toast.makeText(
                    context,
                    "Aktifkan izin notifikasi di pengaturan 📱",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Status Pesanan Kamu 📦")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
