package com.example.miniproject.data

import com.example.miniproject.model.Notification

object NotificationManager {

    private val notifications = mutableListOf<Notification>()
    private var notificationIdCounter = 1

    // Admin kirim notifikasi
    fun sendNotification(title: String, message: String) {
        val notification = Notification(
            id = notificationIdCounter++,
            title = title,
            message = message
        )
        notifications.add(0, notification) // Tambah di awal list (terbaru di atas)
    }

    // User ambil semua notifikasi
    fun getAllNotifications(): List<Notification> {
        return notifications.toList()
    }

    // Ambil notifikasi terbaru (untuk dialog saat login)
    fun getLatestNotification(): Notification? {
        return notifications.firstOrNull()
    }

    // Load dummy notifications (opsional)
    fun loadDummyNotifications() {
        if (notifications.isEmpty()) {
            notifications.addAll(
                listOf(
                    Notification(
                        id = notificationIdCounter++,
                        title = "🎉 Selamat Datang di Agro Shop!",
                        message = "Terima kasih telah bergabung. Dapatkan diskon 10% untuk pembelian pertama!",
                        createdAt = "2025-01-15 10:00"
                    ),
                    Notification(
                        id = notificationIdCounter++,
                        title = "🔥 Flash Sale Hari Ini!",
                        message = "Diskon hingga 50% untuk semua produk pupuk organik. Jangan sampai kehabisan!",
                        createdAt = "2025-01-16 08:30"
                    ),
                    Notification(
                        id = notificationIdCounter++,
                        title = "🚚 Gratis Ongkir!",
                        message = "Gratis ongkir untuk pembelian minimal Rp 200.000. Promo terbatas!",
                        createdAt = "2025-01-17 09:15"
                    )
                )
            )
        }
    }
}