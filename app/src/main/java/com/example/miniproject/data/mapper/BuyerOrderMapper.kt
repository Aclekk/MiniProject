package com.example.miniproject.data.mapper

import com.example.miniproject.data.Order
import com.example.miniproject.data.model.BuyerOrderResponse
import com.example.miniproject.model.Product

fun BuyerOrderResponse.toOrderModel(): Order {

    // 🔧 dummy product karena buyer order API tidak kirim detail product
    val dummyProduct = Product(
        id = 0,
        name = "Produk",
        description = "",
        price = this.totalAmount ?: 0.0,
        stock = 0,
        imageUrl = null,
        categoryId = 0,
        categoryName = "",
        createdAt = ""
    )

    return Order(
        id = this.id,
        products = listOf(dummyProduct),
        totalPrice = this.totalAmount ?: 0.0,
        status = this.status ?: "pending",
        paymentMethod = this.paymentMethod ?: "-",
        address = this.shippingAddress ?: "-"
    )
}
