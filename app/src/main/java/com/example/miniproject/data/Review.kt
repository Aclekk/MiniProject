package com.example.miniproject.data

data class Review(
    val productId: Int,
    val userName: String,
    val rating: Float,
    val comment: String,
    val createdAt: String
)
