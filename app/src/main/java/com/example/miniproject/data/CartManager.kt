package com.example.miniproject.data

import com.example.miniproject.model.Order
import com.example.miniproject.model.Product
import com.example.miniproject.model.Review

object CartManager {

    // ✅ Ubah menjadi public agar bisa diakses
    val cartItems = mutableListOf<Product>()
    val orders = mutableListOf<Order>()
    val reviews = mutableListOf<Review>()

    // --- CART MANAGEMENT ---
    fun addToCart(product: Product, quantity: Int = 1) {
        // Cek apakah produk sudah ada di cart
        val existingProduct = cartItems.find { it.id == product.id }
        if (existingProduct != null) {
            // Update quantity jika sudah ada
            existingProduct.quantity += quantity
        } else {
            // Tambah produk baru dengan quantity
            val newProduct = product.copy(quantity = quantity)
            cartItems.add(newProduct)
        }
    }

    fun getCartItems(): List<Product> = cartItems

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.price * it.quantity }
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun removeFromCart(product: Product) {
        cartItems.remove(product)
    }

    // --- ORDER MANAGEMENT ---
    fun addOrder(order: Order) {
        orders.add(0, order) // Tambah di awal list
    }

    fun getOrders(): List<Order> = orders

    fun updateOrderStatus(orderId: String, newStatus: String) {
        orders.find { it.id == orderId }?.let { order ->
            // Update status dan timestamp sesuai status baru
            when (newStatus) {
                "Dikemas" -> {
                    orders[orders.indexOf(order)] = order.copy(
                        status = newStatus,
                        packedDate = System.currentTimeMillis()
                    )
                }
                "Dikirim" -> {
                    orders[orders.indexOf(order)] = order.copy(
                        status = newStatus,
                        shippedDate = System.currentTimeMillis()
                    )
                }
                "Selesai" -> {
                    orders[orders.indexOf(order)] = order.copy(
                        status = newStatus,
                        completedDate = System.currentTimeMillis()
                    )
                }
                else -> {
                    orders[orders.indexOf(order)] = order.copy(status = newStatus)
                }
            }
        }
    }

    // --- REVIEW MANAGEMENT ---
    fun addReview(review: Review) {
        reviews.add(0, review) // Tambah di awal list
    }

    fun getAllReviews(): List<Review> = reviews

    fun getReviewsByProduct(productId: String): List<Review> {
        return reviews.filter { it.productId == productId }
    }
}