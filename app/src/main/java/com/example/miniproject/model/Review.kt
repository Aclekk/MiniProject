package com.example.miniproject.model

data class Review(
    val id: Int,
    val userId: Int,
    val userName: String,
    val rating: Float, // 1.0 - 5.0
    val comment: String,
    val createdAt: String
)