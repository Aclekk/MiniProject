package com.example.miniproject.data

import com.example.miniproject.model.Order

object OrderManager {

    val allOrders = mutableListOf<Order>()

    fun addOrder(order: Order) {
        allOrders.add(0, order)
        // Sync dengan CartManager
        if (!CartManager.orders.contains(order)) {
            CartManager.orders.add(0, order)
        }
    }

    fun getOrdersByStatus(status: String): List<Order> {
        return allOrders.filter { it.status == status }
    }

    fun getOrdersByUser(userId: String): List<Order> {
        return allOrders.filter { it.userId == userId }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        allOrders.find { it.id == orderId }?.let { order ->
            val index = allOrders.indexOf(order)
            when (newStatus) {
                "Dikemas" -> {
                    allOrders[index] = order.copy(
                        status = newStatus,
                        packedDate = System.currentTimeMillis()
                    )
                }
                "Dikirim" -> {
                    allOrders[index] = order.copy(
                        status = newStatus,
                        shippedDate = System.currentTimeMillis()
                    )
                }
                "Selesai" -> {
                    allOrders[index] = order.copy(
                        status = newStatus,
                        completedDate = System.currentTimeMillis()
                    )
                }
                else -> {
                    allOrders[index] = order.copy(status = newStatus)
                }
            }
            // Sync dengan CartManager
            CartManager.updateOrderStatus(orderId, newStatus)
        }
    }
}