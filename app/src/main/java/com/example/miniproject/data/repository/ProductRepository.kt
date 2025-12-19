package com.example.miniproject.data.repository

import com.example.miniproject.data.api.ApiService
import com.example.miniproject.model.Product

class ProductRepository(
    private val apiService: ApiService
) {

    suspend fun getProducts(): List<Product> {
        val response = apiService.getProducts(
            page = 1,
            limit = 50,
            categoryId = null,
            search = null
        )

        if (response.isSuccessful) {
            return response.body()?.data?.products ?: emptyList()
        } else {
            throw Exception("Gagal load produk: ${response.code()}")
        }
    }

    suspend fun toggleBestSeller(
        token: String,
        productId: Int,
        isBestSeller: Int
    ) {
        val response = apiService.toggleBestSeller(
            token = token,
            productId = productId,
            isBestSeller = isBestSeller
        )

        if (!response.isSuccessful) {
            throw Exception("Gagal update status produk")
        }
    }

    suspend fun deleteProduct(
        token: String,
        productId: Int
    ) {
        val response = apiService.deleteProduct(
            token = token,
            productId = productId
        )

        if (!response.isSuccessful) {
            throw Exception("Gagal hapus produk")
        }
    }
}
