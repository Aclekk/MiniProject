package com.example.miniproject.model

data class SalesReport(
    val date: String,
    val totalOrders: Int,
    val totalRevenue: Double,
    val completedOrders: Int
)

data class DailySales(
    val day: String,
    val sales: Double
)