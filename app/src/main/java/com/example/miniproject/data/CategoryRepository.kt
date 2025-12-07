package com.example.miniproject.data

import android.content.Context
import com.example.miniproject.helpers.DatabaseHelper
import com.example.miniproject.model.Category

object CategoryRepository {

    private lateinit var dbHelper: DatabaseHelper

    fun initialize(context: Context) {
        dbHelper = DatabaseHelper(context.applicationContext)
    }

    fun getCategories(): List<Category> {
        return dbHelper.getAllCategories()
    }

    fun addCategory(name: String, iconResId: Int): Boolean {
        val result = dbHelper.addCategory(name, "Kategori $name", "ic_category")
        return result != -1L
    }

    fun updateCategory(id: Int, name: String): Boolean {
        val result = dbHelper.updateCategory(id, name)
        return result > 0
    }

    fun deleteCategory(id: Int): Boolean {
        val result = dbHelper.deleteCategory(id)
        return result > 0
    }

    fun isCategoryNameExists(name: String): Boolean {
        return dbHelper.isCategoryNameExists(name)
    }
}