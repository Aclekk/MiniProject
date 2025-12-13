package com.example.miniproject.data.mapper

import com.example.miniproject.data.Order
import com.example.miniproject.data.model.BuyerOrderResponse
import com.example.miniproject.data.model.SellerOrderResponse
import com.example.miniproject.model.Product
import com.example.miniproject.data.ProductDataSource

private fun resolveProduct(productId: Int?): Product? {
    val all = ProductDataSource.getAllProducts()
    if (all.isEmpty()) return null
    return productId?.let { id ->
        all.find { it.id == id }
    } ?: all.first()
}

fun BuyerOrderResponse.toOrderModel(): Order {
    val product = resolveProduct(productId)
    val products = product?.let { listOf(it) } ?: emptyList()

    val total = totalAmount ?: ((productPrice ?: 0.0) * (quantity ?: 1))

    return Order(
        id = id,
        status = status ?: "pending",
        paymentMethod = paymentMethod ?: "-",
        address = shippingAddress ?: "-",
        products = products,
        totalPrice = total
    )
}

fun SellerOrderResponse.toOrderModel(): Order {
    val product = resolveProduct(productId)
    val products = product?.let { listOf(it) } ?: emptyList()

    val total = totalAmount ?: ((productPrice ?: 0.0) * (quantity ?: 1))

    return Order(
        id = id,
        status = status ?: "pending",
        paymentMethod = paymentMethod ?: "-",
        address = shippingAddress ?: "-",
        products = products,
        totalPrice = total
    )
}
