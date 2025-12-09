package com.example.miniproject.data.api

data class CheckoutRequest(
    val product_id: Int,
    val quantity: Int,
    val fcm_token: String
)

