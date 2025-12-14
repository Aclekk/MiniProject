package com.example.miniproject.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import com.example.miniproject.data.api.ApiClient

object ApiClient {

    // ✅ PENTING: Ganti dengan IP komputer Anda yang menjalankan PHP server
    private const val BASE_URL = "http://192.168.100.7/agritools_api/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // ✅ FIX: Return String NON-NULLABLE dan handle empty case
    fun getImageUrl(imagePath: String?): String {
        // Kalau null atau empty, return placeholder
        if (imagePath.isNullOrEmpty()) {
            return ""  // Glide akan pakai placeholder/error drawable
        }

        // Kalau sudah full URL (dimulai dengan http)
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath
        }

        // Gabungkan BASE_URL dengan path relatif
        // contoh: "uploads/products/product_123.jpg" -> "http://192.168.100.18/agritools_api/uploads/products/product_123.jpg"
        return BASE_URL + imagePath
    }
}