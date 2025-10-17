package com.example.miniproject.model

import android.net.Uri

/**
 * 🏪 Singleton object untuk menyimpan data profil toko
 * Data ini akan hilang saat app di-restart (sesuai requirement)
 */
object StoreProfile {

    // Data Toko
    var storeName: String = "🌾 Niaga Tani"
    var storeAddress: String = "Jl. Pertanian Sejahtera No. 123, Jakarta Selatan"
    var storeContact: String = "+62 812-3456-7890"
    var storeAbout: String = "Solusi Pertanian Modern Indonesia - Menyediakan produk pertanian berkualitas tinggi untuk petani Indonesia"
    var storePhotoUri: Uri? = null

    /**
     * Reset ke data dummy (dipanggil saat logout atau restart app)
     */
    fun resetToDefault() {
        storeName = "🌾 Niaga Tani"
        storeAddress = "Jl. Pertanian Sejahtera No. 123, Jakarta Selatan"
        storeContact = "+62 812-3456-7890"
        storeAbout = "Solusi Pertanian Modern Indonesia - Menyediakan produk pertanian berkualitas tinggi untuk petani Indonesia"
        storePhotoUri = null
    }

    /**
     * Update profil toko
     */
    fun updateProfile(
        name: String,
        address: String,
        contact: String,
        about: String,
        photoUri: Uri? = null
    ) {
        storeName = name
        storeAddress = address
        storeContact = contact
        storeAbout = about
        if (photoUri != null) {
            storePhotoUri = photoUri
        }
    }
}