package com.example.miniproject.api

import com.example.miniproject.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // Auth endpoints
    @POST("auth.php?action=login")
    fun login(@Body loginRequest: LoginRequest): Call<AuthResponse>

    @POST("auth.php?action=register")
    fun register(@Body registerRequest: RegisterRequest): Call<AuthResponse>

    // Products endpoints
    @GET("products.php")
    fun getAllProducts(): Call<ProductListResponse>

    @GET("products.php")
    fun getProductById(@Query("id") id: Int): Call<ProductResponse>

    @POST("products.php")
    fun createProduct(@Body product: ProductRequest): Call<ProductResponse>

    @PUT("products.php")
    fun updateProduct(@Body product: ProductRequest): Call<ProductResponse>

    @DELETE("products.php")
    fun deleteProduct(@Query("id") id: Int): Call<ProductResponse>

    // Categories endpoints
    @GET("categories.php")
    fun getAllCategories(): Call<CategoryResponse>

    @POST("categories.php")
    fun createCategory(@Body category: CategoryRequest): Call<CategoryResponse>

    @POST("auth.php?action=register")
    fun registerUser(@Body userRequest: UserRequest): Call<RegisterResponse>
}