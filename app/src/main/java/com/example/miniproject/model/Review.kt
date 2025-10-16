package com.example.miniproject.model

data class Review(
    val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val userName: String,
    val productId: String,  // ✅ Ubah dari Int ke String
    val productName: String = "",
    val rating: Float,
    val comment: String,
    val reviewDate: Long = System.currentTimeMillis()
)