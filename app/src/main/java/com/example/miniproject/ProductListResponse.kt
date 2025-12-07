package com.example.miniproject.data.model

import com.example.miniproject.model.Product
import com.squareup.moshi.Json

data class ProductListResponse(
    @Json(name = "products")
    val products: List<Product>,

    @Json(name = "total")
    val total: Int,

    @Json(name = "limit")
    val limit: Int,

    @Json(name = "offset")
    val offset: Int
)
