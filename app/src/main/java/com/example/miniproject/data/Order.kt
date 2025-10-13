package com.example.miniproject.data

import com.example.miniproject.model.Product

// 💸 Kelas data untuk simulasi pesanan
data class Order(
    val id: Int,
    val products: List<Product>,
    val totalPrice: Double,
    var status: String = "Belum Bayar", // Belum Bayar | Dikemas | Dikirim | Selesai
    val paymentMethod: String = "",
    val address: String = ""
)
