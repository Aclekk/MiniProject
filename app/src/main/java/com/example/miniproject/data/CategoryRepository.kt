package com.example.miniproject.data

import android.util.Log
import com.example.miniproject.R
import com.example.miniproject.model.Category

object CategoryRepository {

    // Gunakan mutableList biar bisa ditambah/dihapus
    private val categories = mutableListOf(
        Category(id = 1, categoryName = "Peralatan", createdAt = "2025-01-01", icon = R.drawable.peralatan),
        Category(id = 2, categoryName = "Pupuk", createdAt = "2025-01-01", icon = R.drawable.pupukk),
        Category(id = 3, categoryName = "Benih", createdAt = "2025-01-01", icon = R.drawable.benihh),
        Category(id = 4, categoryName = "Alat Pertanian", createdAt = "2025-01-01", icon = R.drawable.peralatan),
        Category(id = 5, categoryName = "Pestisida", createdAt = "2025-01-01", icon = R.drawable.pest)
    )

    // ✅ Ambil semua kategori
    fun getCategories(): MutableList<Category> = categories

    // ✅ Ambil kategori by ID
    fun getCategoryById(id: Int): Category? {
        return categories.find { it.id == id }
    }

    // ✅ Tambahkan kategori baru oleh admin
    fun addCategory(name: String, iconResId: Int = R.drawable.ic_category): Boolean {
        if (categories.any { it.categoryName.equals(name, ignoreCase = true) }) {
            Log.w("CategoryRepository", "⚠️ Category '$name' already exists")
            return false
        }

        val newId = (categories.maxOfOrNull { it.id } ?: 0) + 1
        val currentDate = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        val newCategory = Category(
            id = newId,
            categoryName = name,
            createdAt = currentDate,
            icon = iconResId
        )
        categories.add(newCategory)
        Log.d("CategoryRepository", "✅ Category added: $name (ID: $newId)")
        return true
    }

    // ✅ Update kategori yang sudah ada
    fun updateCategory(id: Int, newName: String, iconResId: Int? = null): Boolean {
        val categoryIndex = categories.indexOfFirst { it.id == id }
        if (categoryIndex == -1) {
            Log.w("CategoryRepository", "⚠️ Category ID $id not found")
            return false
        }

        // Cek duplikat nama (kecuali nama sendiri)
        if (categories.any { it.categoryName.equals(newName, ignoreCase = true) && it.id != id }) {
            Log.w("CategoryRepository", "⚠️ Category name '$newName' already exists")
            return false
        }

        val oldCategory = categories[categoryIndex]
        val updatedCategory = oldCategory.copy(
            categoryName = newName,
            icon = iconResId ?: oldCategory.icon
        )

        categories[categoryIndex] = updatedCategory
        Log.d("CategoryRepository", "✅ Category updated: ${oldCategory.categoryName} → $newName")
        return true
    }

    // ✅ Hapus kategori
    fun deleteCategory(id: Int): Boolean {
        val category = categories.find { it.id == id }
        if (category == null) {
            Log.w("CategoryRepository", "⚠️ Category ID $id not found")
            return false
        }

        categories.removeAll { it.id == id }
        Log.d("CategoryRepository", "🗑️ Category deleted: ${category.categoryName}")
        return true
    }

    // ✅ Cek apakah nama kategori sudah ada
    fun isCategoryNameExists(name: String, excludeId: Int? = null): Boolean {
        return categories.any {
            it.categoryName.equals(name, ignoreCase = true) &&
                    (excludeId == null || it.id != excludeId)
        }
    }

    // 🧹 Clear all categories (untuk testing)
    fun clearAll() {
        categories.clear()
        Log.d("CategoryRepository", "🧹 All categories cleared")
    }
}