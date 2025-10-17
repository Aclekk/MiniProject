package com.example.miniproject.data

import com.example.miniproject.R
import com.example.miniproject.model.Category

object CategoryRepository {
    fun getCategories(): List<Category> {
        return listOf(
            Category(id = 1, categoryName = "Peralatan", createdAt = "2025-01-01", icon = R.drawable.peralatan),
            Category(id = 2, categoryName = "Pupuk", createdAt = "2025-01-01", icon = R.drawable.pupukk),
            Category(id = 3, categoryName = "Benih", createdAt = "2025-01-01", icon = R.drawable.benihh),
            Category(id = 4, categoryName = "Alat Pertanian", createdAt = "2025-01-01", icon = R.drawable.peralatan),
            Category(id = 5, categoryName = "Pestisida", createdAt = "2025-01-01", icon = R.drawable.pest)
        )
    }
}