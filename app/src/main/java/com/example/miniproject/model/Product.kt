package com.example.miniproject.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "category_id") val categoryId: Int?,
    val stock: Int,
    @Json(name = "category_name") val categoryName: String?,
    @Json(name = "created_at") val createdAt: String?,
    val imageResId: Int? = null
) : Parcelable
