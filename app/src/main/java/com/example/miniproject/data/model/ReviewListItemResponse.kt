package com.example.miniproject.data.model

import com.squareup.moshi.Json

data class ReviewListItemResponse(
    val id: Int,

    @Json(name = "order_id")
    val orderId: Int,

    @Json(name = "product_id")
    val productId: Int,

    @Json(name = "user_id")
    val userId: Int,

    val rating: Int,
    val comment: String?,

    @Json(name = "is_anonymous")
    val isAnonymous: Int,

    @Json(name = "created_at")
    val createdAt: String,

    @Json(name = "product_name")
    val productName: String?,

    @Json(name = "product_image")
    val productImage: String?,

    @Json(name = "order_number")
    val orderNumber: String?,

    // ada saat list_for_seller
    @Json(name = "user_name")
    val userName: String? = null,

    @Json(name = "user_image")
    val userImage: String? = null
)
