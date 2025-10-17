package com.example.miniproject.data

import com.example.miniproject.model.Product

data class Order(
    val id: Int,
    val products: List<Product>,
    val totalPrice: Double,
    var status: String = "Belum Bayar", // Belum Bayar | Dikemas | Dikirim | Selesai
    val paymentMethod: String = "",
    val address: String = "",
    var hasReview: Boolean = false // 🆕 Tracking apakah sudah kasih review
)