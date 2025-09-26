package com.example.miniproject.model

import com.squareup.moshi.Json

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    @field:Json(name = "created_at") val createdAt: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val role: String = "user"
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)
// Tambahkan ke file model yang sudah ada

data class UserRequest(
    val username: String,
    val password: String,
    val email: String,
    val role: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    @field:Json(name = "user_id") val userId: Int? = null
)