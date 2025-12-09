package com.example.miniproject.data.api

import com.google.gson.annotations.SerializedName

data class CreateOrderResult(
    @SerializedName("order_id")    val orderId: Int,
    @SerializedName("product_id")  val productId: Int,
    @SerializedName("seller_id")   val sellerId: Int,
    val quantity: Int,
    @SerializedName("total_price") val totalPrice: Double,
    val status: String
)
