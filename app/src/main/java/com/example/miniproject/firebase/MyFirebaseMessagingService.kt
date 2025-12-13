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
import kotlin.math.abs

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
        const val CHANNEL_ID = "order_updates"

        const val ACTION_REFRESH_ORDERS = "com.example.miniproject.REFRESH_ORDERS"
        const val ACTION_REFRESH_BUYER_ORDERS = "com.example.miniproject.REFRESH_BUYER_ORDERS"
        const val ACTION_ORDER_STATUS_CHANGED = "com.example.miniproject.ORDER_STATUS_CHANGED"

        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_STATUS = "status"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM Token refreshed: $token")

        val prefs = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        val authToken = prefs.getString("token", null)
        if (!authToken.isNullOrEmpty()) {
            uploadTokenToServer(token, authToken)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"] ?: ""
        val title = message.notification?.title ?: message.data["title"] ?: "Notifikasi Baru"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val orderId = message.data["order_id"]?.toIntOrNull() ?: 0
        val status = message.data["status"] ?: ""

        Log.d(TAG, "Type=$type orderId=$orderId status=$status")

        showNotification(title, body, orderId)

        when (type) {
            "order_status" -> {
                sendBroadcast(Intent(ACTION_REFRESH_ORDERS).apply {
                    putExtra(EXTRA_ORDER_ID, orderId)
                    putExtra(EXTRA_STATUS, status)
                })
                sendBroadcast(Intent(ACTION_REFRESH_BUYER_ORDERS).apply {
                    putExtra(EXTRA_ORDER_ID, orderId)
                    putExtra(EXTRA_STATUS, status)
                })
                sendBroadcast(Intent(ACTION_ORDER_STATUS_CHANGED).apply {
                    putExtra(EXTRA_ORDER_ID, orderId)
                    putExtra(EXTRA_STATUS, status)
                })
            }
            "new_order" -> {
                sendBroadcast(Intent(ACTION_REFRESH_ORDERS))
            }
            else -> {
                sendBroadcast(Intent(ACTION_REFRESH_ORDERS))
                sendBroadcast(Intent(ACTION_REFRESH_BUYER_ORDERS))
            }
        }
    }

    private fun showNotification(title: String, message: String, orderId: Int) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi update pesanan"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ORDER_ID, orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            orderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // kalau orderId 0, bikin id unik biar notif nggak ketimpa
        val notificationId = if (orderId != 0) abs(orderId) else abs(System.currentTimeMillis().toInt())

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // kalau masih error, ganti: android.R.drawable.ic_dialog_info
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(notificationId, notif)
    }

    private fun uploadTokenToServer(fcmToken: String, authToken: String) {
        Log.d(TAG, "Token uploaded to server (TODO): $fcmToken")
    }
}
