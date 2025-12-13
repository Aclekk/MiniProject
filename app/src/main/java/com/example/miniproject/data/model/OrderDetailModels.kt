package com.example.miniproject.data.model

import com.squareup.moshi.Json // ← GANTI import ini (bukan SerializedName)

data class OrderDetailData(
    @Json(name = "order") val order: OrderDetail? = null,
    @Json(name = "items") val items: List<OrderItemDetail> = emptyList()
)

data class OrderDetail(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "order_number") val orderNumber: String? = null,
    @Json(name = "status") val status: String? = null
)

data class OrderItemDetail(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "product_id") val productId: Int? = null, // ← GANTI @SerializedName jadi @Json
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "quantity") val quantity: Int? = null,
    @Json(name = "price") val price: Double? = null,
    @Json(name = "subtotal") val subtotal: Double? = null
)