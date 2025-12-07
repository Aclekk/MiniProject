package com.example.miniproject

import android.app.Application
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.ProductDataSource

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize database helpers
        CategoryRepository.initialize(this)
        ProductDataSource.initialize(this)
    }
}