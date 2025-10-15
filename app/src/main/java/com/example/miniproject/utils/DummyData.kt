package com.example.miniproject.utils

import com.example.miniproject.R
import com.example.miniproject.model.Product

object DummyData {

    /**
     * ✅ SINGLE SOURCE OF TRUTH untuk semua dummy products
     * Dipanggil oleh ProductsFragment DAN HomeFragment
     */
    fun getDummyProducts(): List<Product> {
        return listOf(
            // ===== PRODUK DARI PRODUCTSFRAGMENT (HOME) =====
            Product(
                id = 1,
                name = "Cangkul Premium",
                price = 150000.0,
                description = "Cangkul baja berkualitas tinggi untuk mengolah tanah",
                imageUrl = null,
                imageResId = R.drawable.cangkul,
                categoryId = 1,
                stock = 50,
                categoryName = "Peralatan",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 2,
                name = "Pupuk Organik 25kg",
                price = 200000.0,
                description = "Pupuk alami ramah lingkungan berkualitas premium",
                imageUrl = null,
                imageResId = R.drawable.pupuk,
                categoryId = 2,
                stock = 30,
                categoryName = "Pupuk",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 3,
                name = "Benih Padi Premium",
                price = 50000.0,
                description = "Benih unggul hasil seleksi terbaik",
                imageUrl = null,
                imageResId = R.drawable.benih,
                categoryId = 3,
                stock = 100,
                categoryName = "Benih",
                createdAt = "2025-01-01"
            ),

            // ===== PRODUK TAMBAHAN DARI HOMEFRAGMENT (PRODUCTS) =====
            Product(
                id = 4,
                name = "Traktor Mini",
                price = 5000000.0,
                description = "Traktor mini untuk pertanian skala kecil",
                imageUrl = null,
                imageResId = R.drawable.traktor,
                categoryId = 4,
                stock = 5,
                categoryName = "Alat Pertanian",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 5,
                name = "Rotavator Tangan",
                price = 950000.0,
                description = "Alat putar penggembur tanah manual atau elektrik",
                imageUrl = null,
                imageResId = R.drawable.rotavator,
                categoryId = 1,
                stock = 7,
                categoryName = "Peralatan",
                createdAt = "2025-01-02"
            ),
            Product(
                id = 6,
                name = "Sekop Kebun",
                price = 85000.0,
                description = "Sekop multifungsi untuk menggali dan memindahkan material",
                imageUrl = null,
                imageResId = R.drawable.sekop,
                categoryId = 1,
                stock = 35,
                categoryName = "Peralatan",
                createdAt = "2025-01-02"
            ),
            Product(
                id = 7,
                name = "Selang Irigasi 20m",
                price = 120000.0,
                description = "Selang elastis kualitas premium untuk sistem penyiraman",
                imageUrl = null,
                imageResId = R.drawable.selang,
                categoryId = 1,
                stock = 40,
                categoryName = "Peralatan",
                createdAt = "2025-01-02"
            ),
            Product(
                id = 8,
                name = "Arit Tajam",
                price = 55000.0,
                description = "Arit baja tajam untuk panen padi dan potong rumput",
                imageUrl = null,
                imageResId = R.drawable.arit,
                categoryId = 1,
                stock = 20,
                categoryName = "Peralatan",
                createdAt = "2025-01-02"
            )
        )
    }
}