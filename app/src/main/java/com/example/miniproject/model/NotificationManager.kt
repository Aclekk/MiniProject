package com.example.miniproject.data

import com.example.miniproject.model.Notification

object NotificationManager {
    val notifications = mutableListOf<Notification>()

    fun addNotification(notification: Notification) {
        notifications.add(notification)
    }

    fun getAllNotifications(): List<Notification> {
        return notifications.sortedByDescending { it.createdAt }
    }
}