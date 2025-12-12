package com.example.miniproject.data.api

data class CheckoutRequest(
    val product_id: Int,
    val quantity: Int,

    val shipping_address: String,
    val shipping_city: String,
    val shipping_province: String,
    val shipping_postal_code: String,

    val recipient_name: String,
    val recipient_phone: String,

    val shipping_cost: Double,
    val note: String? = null,

    // wajib: "bank_transfer" / "ewallet"
    val payment_method: String
)
