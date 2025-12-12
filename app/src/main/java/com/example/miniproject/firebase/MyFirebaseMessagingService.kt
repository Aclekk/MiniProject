package com.example.miniproject.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.miniproject.MainActivity
import com.example.miniproject.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val ACTION_REFRESH_ORDERS = "com.example.miniproject.ACTION_REFRESH_ORDERS"
    }

    private val tag = "FCM"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "New FCM token: $token")

        val prefs = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("device_fcm_token", token)
            .apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(tag, "From: ${message.from}")
        Log.d(tag, "Data: ${message.data}")
        Log.d(tag, "Notification: ${message.notification}")

        val data = message.data
        val type = data["type"] ?: ""

        val title = message.notification?.title
            ?: data["title"]
            ?: "Niaga Tani"

        val body = message.notification?.body
            ?: data["body"]
            ?: "Ada notifikasi baru."

        // Tampilkan notif
        showNotification(title, body)

        // 🔥 Trigger refresh list order kalau relevan
        if (type == "new_order" || type == "order_status") {
            sendBroadcast(Intent(ACTION_REFRESH_ORDERS))
            Log.d(tag, "Broadcast refresh orders sent")
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "niagatani_default_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NiagaTani Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else PendingIntent.FLAG_ONE_SHOT

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
