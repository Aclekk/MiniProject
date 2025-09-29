package com.example.miniproject.model

import com.squareup.moshi.Json

data class CategoryRequest(
    @Json(name = "category_name")
    val categoryName: String
)