package com.example.miniproject.data

import com.example.miniproject.model.Category

object CategoryRepository {
    // Sumber data tunggal kategori
    fun getCategories(): List<Category> = listOf(
        Category(1, "Pertanian", "2025-01-01"),
        Category(2, "Pupuk", "2025-01-01"),
        Category(3, "Benih", "2025-01-01"),
        Category(4, "Peralatan", "2025-01-01"),
        Category(5, "Pestisida", "2025-01-01")
    )
}
