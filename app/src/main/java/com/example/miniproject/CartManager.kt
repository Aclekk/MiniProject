package com.example.miniproject.data

import com.example.miniproject.model.Product

object CartManager {
    val cartItems = mutableListOf<Product>()
    val orders = mutableListOf<Order>()
    val reviews = mutableListOf<Review>()

    fun addToCart(product: Product) {
        cartItems.add(product)
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun addReview(review: Review) {
        reviews.add(review)
    }

    fun getReviewsByProduct(productId: Int): List<Review> {
        return reviews.filter { it.productId == productId }
    }
}
