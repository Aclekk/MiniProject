package com.example.miniproject.data

import com.example.miniproject.model.Product

data class Order(
    val id: Int,
    var status: String,
    val paymentMethod: String,
    val address: String,
    val products: List<Product>,
    val totalPrice: Double,
    var hasReview: Boolean = false
)