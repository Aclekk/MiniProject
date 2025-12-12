package com.example.miniproject.data.mapper

import com.example.miniproject.data.Order
import com.example.miniproject.data.model.SellerOrderResponse
import com.example.miniproject.model.Product
import com.example.miniproject.util.normalizeDbStatus

fun SellerOrderResponse.toOrderModel(): Order {
    val dummyProduct = Product(
        id = (this.productId ?: 0),
        name = (this.productName ?: "Produk"),
        description = "",
        price = (this.productPrice ?: 0.0),
        stock = 0,
        imageUrl = null,
        categoryId = 0,
        categoryName = "",
        createdAt = ""
    )

    return Order(
        id = this.id,
        products = listOf(dummyProduct),
        totalPrice = (this.totalAmount ?: 0.0),
        status = normalizeDbStatus(this.status), // ✅ penting
        paymentMethod = (this.paymentMethod ?: "-"),
        address = (this.shippingAddress ?: "-")
    )
}
