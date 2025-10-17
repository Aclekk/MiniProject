package com.example.miniproject.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int, // tetap val karena ID tidak perlu diubah
    var name: String,
    var price: Double,
    var description: String?,
    @Json(name = "image_url") var imageUrl: String?,
    @Json(name = "category_id") var categoryId: Int?,
    var stock: Int,
    @Json(name = "category_name") var categoryName: String?,
    @Json(name = "created_at") var createdAt: String?,
    var imageResId: Int? = null
) : Parcelable
