package com.example.miniproject.model

import com.squareup.moshi.Json


data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "category_id") val categoryId: Int?,
    val stock: Int,
    @Json(name = "category_name") val categoryName: String?,
    @Json(name = "created_at") val createdAt: String?
)


data class ProductRequest(
    val name: String,
    val price: Double,
    val description: String,
    @field:Json(name = "image_url") val imageUrl: String,
    @field:Json(name = "category_id") val categoryId: Int,
    val stock: Int
)

data class ProductResponse(
    val success: Boolean,
    val message: String?, // Add ? here
    val data: List<Product>? = null,
    @field:Json(name = "product_id") val productId: Int? = null
)