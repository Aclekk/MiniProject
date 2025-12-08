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
import com.example.miniproject.data.api.ApiClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "FCM"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "New FCM token: $token")

        // Simpan ke SharedPreferences
        val prefs = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // Kalau user sudah login (punya bearer token), kirim ke backend
        val authToken = prefs.getString("token", null)

        if (!authToken.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val body = mapOf("fcm_token" to token)
                    val response = ApiClient.apiService.updateFcmToken(
                        "Bearer $authToken",
                        body
                    )

                    Log.d(
                        tag,
                        "updateFcmToken -> code=${response.code()} msg=${response.body()?.message}"
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Failed to update FCM token", e)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Niaga Tani"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Ada notifikasi baru nih."

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "niagatani_default_channel"
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            flags
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)   // ganti pakai icon custom kalau mau
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
