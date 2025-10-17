package com.example.miniproject.model

import java.text.SimpleDateFormat
import java.util.*

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val createdAt: String = SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    ).format(Date())
)