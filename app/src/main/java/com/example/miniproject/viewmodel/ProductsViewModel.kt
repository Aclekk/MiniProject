package com.example.miniproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.repository.ProductRepository
import com.example.miniproject.model.Product
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadProducts() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val result = repository.getProducts()
                _products.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleBestSeller(
        token: String,
        productId: Int,
        currentStatus: Int
    ) {
        viewModelScope.launch {
            try {
                val newStatus = if (currentStatus == 1) 0 else 1
                repository.toggleBestSeller(
                    token = token,
                    productId = productId,
                    isBestSeller = newStatus
                )
                // refresh data
                loadProducts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteProduct(
        token: String,
        productId: Int
    ) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(
                    token = token,
                    productId = productId
                )
                // refresh data
                loadProducts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
