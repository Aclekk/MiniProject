package com.example.miniproject.data

data class User(
    var name: String,
    var email: String,
    var address: String,
    var phone: String,
    var profileImageUri: String? = null
)

object UserManager {
    var currentUser = User(
        name = "Rachen Ziyad 🌾",
        email = "rachenkeren@example.com",
        address = "Jl. Sawah Hijau No. 12, Bogor",
        phone = "081234567890",
        profileImageUri = null
    )
}
