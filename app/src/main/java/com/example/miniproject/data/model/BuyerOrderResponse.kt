package com.example.miniproject.data.model

import com.squareup.moshi.Json

data class BuyerOrderResponse(
    val id: Int,

    @Json(name = "order_number")
    val orderNumber: String? = null,

    @Json(name = "product_id")
    val productId: Int? = null,

    @Json(name = "product_name")
    val productName: String? = null,

    @Json(name = "product_price")
    val productPrice: Double? = null,

    @Json(name = "product_image")
    val productImage: String? = null,

    val quantity: Int? = null,

    @Json(name = "total_amount")
    val totalAmount: Double? = null,

    val status: String? = null,

    @Json(name = "payment_method")
    val paymentMethod: String? = null,

    @Json(name = "shipping_address")
    val shippingAddress: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null
)
