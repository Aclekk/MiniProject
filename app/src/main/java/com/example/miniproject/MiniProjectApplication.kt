package com.example.miniproject

import android.app.Application
import com.example.miniproject.model.DummyDataRepository

class MiniProjectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize dummy data saat aplikasi pertama kali dijalankan
        DummyDataRepository.initializeDummyData()
    }
}