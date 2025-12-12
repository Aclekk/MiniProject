package com.example.miniproject.data.model

data class BuyerOrderResponse(
    val id: Int,
    val orderNumber: String? = null,
    val totalAmount: Double? = null,
    val status: String? = null,
    val paymentMethod: String? = null,
    val shippingAddress: String? = null
)
