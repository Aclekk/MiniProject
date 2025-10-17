package com.example.miniproject.model

import android.net.Uri

/**
 * 👤 Model untuk menyimpan data profil USER (bukan admin)
 * Data ini terpisah dari StoreProfile (yang khusus admin)
 */
object UserProfile {
    var userName: String = "User"
    var userEmail: String = "user@example.com"
    var userAddress: String = "Jl. Sawah Indah No. 7, Tangerang"
    var userPhotoUri: Uri? = null // ✅ Foto profil user sendiri

    /**
     * Update profil user
     */
    fun updateProfile(
        name: String,
        email: String,
        address: String,
        photoUri: Uri?
    ) {
        userName = name
        userEmail = email
        userAddress = address
        if (photoUri != null) {
            userPhotoUri = photoUri
        }
    }

    /**
     * Reset ke default (saat logout atau restart app)
     */
    fun reset() {
        userName = "User"
        userEmail = "user@example.com"
        userAddress = "Jl. Sawah Indah No. 7, Tangerang"
        userPhotoUri = null
    }
}