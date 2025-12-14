package com.example.miniproject.data.model

import com.squareup.moshi.Json

data class SalesReportData(
    @Json(name = "summary") val summary: SalesReportSummary,
    @Json(name = "reports") val reports: List<SalesReportRow>
)

data class SalesReportSummary(
    @Json(name = "total_orders") val totalOrders: Int,
    @Json(name = "total_revenue") val totalRevenue: Double,
    @Json(name = "total_items_sold") val totalItemsSold: Int,

    @Json(name = "today_orders") val todayOrders: Int,
    @Json(name = "today_revenue") val todayRevenue: Double,
    @Json(name = "today_items_sold") val todayItemsSold: Int,

    @Json(name = "month_orders") val monthOrders: Int,
    @Json(name = "month_revenue") val monthRevenue: Double,
    @Json(name = "month_items_sold") val monthItemsSold: Int,
)

data class SalesReportRow(
    @Json(name = "id") val id: Int,
    @Json(name = "report_date") val reportDate: String,
    @Json(name = "total_orders") val totalOrders: Int,
    @Json(name = "total_revenue") val totalRevenue: Double,
    @Json(name = "total_items_sold") val totalItemsSold: Int,
)
