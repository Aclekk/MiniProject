package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.databinding.ItemNotificationHistoryBinding
import com.example.miniproject.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationHistoryAdapter(
    private val notifications: List<Notification>
) : RecyclerView.Adapter<NotificationHistoryAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(private val binding: ItemNotificationHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

            binding.tvNotificationTitle.text = notification.title
            binding.tvNotificationMessage.text = notification.message
            binding.tvNotificationDate.text = dateFormat.format(Date(notification.createdAt))
            binding.tvRecipientCount.text = "Terkirim ke: ${notification.recipientCount} user"

            // Set badge type
            binding.tvNotificationType.text = when (notification.type) {
                "BROADCAST" -> "📢 Broadcast"
                "PERSONAL" -> "👤 Personal"
                "PROMO" -> "🎉 Promo"
                else -> notification.type
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size
}