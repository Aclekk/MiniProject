package com.example.miniproject.data

data class Review(
    val orderId: Int = 0,
    val productId: Int,
    val productName: String,
    val productImageUrl: String? = null,
    val rating: Float,
    val comment: String,
    val userName: String = "User",
    val createdAt: String = java.text.SimpleDateFormat(
        "yyyy-MM-dd",
        java.util.Locale.getDefault()
    ).format(java.util.Date())
)