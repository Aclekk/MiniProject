package com.example.miniproject.data

import com.example.miniproject.model.Product

// 🛒 Object global yang menyimpan data sementara di memori (dummy)
object CartManager {
    val cartItems = mutableListOf<Product>()   // Produk di keranjang
    val orders = mutableListOf<Order>()        // Pesanan yang dibuat

    fun addToCart(product: Product) {
        cartItems.add(product)
    }

    fun clearCart() {
        cartItems.clear()
    }
}
