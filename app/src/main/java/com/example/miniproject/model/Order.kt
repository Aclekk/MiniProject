package com.example.miniproject.model

data class Order(
    val id: String,  // ✅ Ubah dari Int ke String
    val userId: String,
    val userName: String,
    val items: List<String>?,  // List nama produk sebagai String
    val products: List<Product>,  // List produk lengkap
    var status: String,
    val totalPrice: Double,
    val orderDate: Long,
    val paymentStatus: String = "UNPAID",
    val shippingAddress: String = "",
    val phoneNumber: String = "",
    val paymentMethod: String = "",
    val packedDate: Long? = null,
    val shippedDate: Long? = null,
    val completedDate: Long? = null
)