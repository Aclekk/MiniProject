package com.example.miniproject.model

data class Category(
    val categoryId: String,
    val categoryName: String,
    val stock: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)