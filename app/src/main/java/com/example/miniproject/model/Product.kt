package com.example.miniproject.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val description: String,
    var quantity: Int = 1  // ✅ Tambahkan property quantity dengan default 1
)