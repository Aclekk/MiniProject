package com.example.miniproject.data

import android.content.Context
import com.example.miniproject.helpers.DatabaseHelper
import com.example.miniproject.model.Product

object ProductDataSource {

    private lateinit var dbHelper: DatabaseHelper

    fun initialize(context: Context) {
        dbHelper = DatabaseHelper(context.applicationContext)
    }

    fun getAllProducts(): MutableList<Product> {
        return dbHelper.getAllProducts().toMutableList()
    }

    fun getProductsByCategory(categoryId: Int): List<Product> {
        return dbHelper.getProductsByCategory(categoryId)
    }

    fun addProduct(
        name: String,
        price: Double,
        description: String,
        imageUrl: String?,
        categoryId: Int,
        stock: Int
    ): Long {
        return dbHelper.addProduct(name, price, description, imageUrl, categoryId, stock)
    }

    fun updateProduct(
        id: Int,
        name: String,
        price: Double,
        description: String,
        imageUrl: String?,
        categoryId: Int,
        stock: Int
    ): Boolean {
        val result = dbHelper.updateProduct(id, name, price, description, imageUrl, categoryId, stock)
        return result > 0
    }

    fun deleteProduct(product: Product): Boolean {
        val result = dbHelper.deleteProduct(product.id)
        return result > 0
    }

    // Untuk backward compatibility dengan kode lama
    fun loadDummyData() {
        // Tidak perlu lagi, data sudah di database
    }
}