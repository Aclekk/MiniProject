package com.example.miniproject.model

import com.squareup.moshi.Json

data class User(
    val id: String,
    val name: String,
    val email: String
)