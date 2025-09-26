package com.example.miniproject.model

import com.squareup.moshi.Json

data class Category(
    val id: Int,
    @field:Json(name = "category_name") val categoryName: String,
    @field:Json(name = "created_at") val createdAt: String
)

data class CategoryRequest(
    @field:Json(name = "category_name") val categoryName: String
)

data class CategoryResponse(
    val success: Boolean,
    val message: String,
    val data: List<Category>? = null,
    @field:Json(name = "category_id") val categoryId: Int? = null
)
data class ProductListResponse(
    val success: Boolean,
    val message: String?,
    val data: List<Product>?
)
