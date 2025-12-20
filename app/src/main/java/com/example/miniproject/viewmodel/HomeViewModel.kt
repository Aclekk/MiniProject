package com.example.miniproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.api.ApiService
import com.example.miniproject.data.repository.CategoryRepository
import com.example.miniproject.model.Product
import com.example.miniproject.model.Category
import com.example.miniproject.data.model.PromoApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

/**
 * ViewModel untuk ProductsFragment (UI Home)
 * Menangani 4 data sources: Products, Categories, Store Info, Promos
 */
class HomeViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // ===== PRODUCTS =====
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    // ===== CATEGORIES =====
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // ===== STORE INFO =====
    private val _storeInfo = MutableLiveData<StoreInfoData>()
    val storeInfo: LiveData<StoreInfoData> = _storeInfo

    // ===== PROMOS =====
    private val _promos = MutableLiveData<List<PromoApi>>()
    val promos: LiveData<List<PromoApi>> = _promos

    // ===== UI STATE =====
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // ==================== LOAD BEST SELLER PRODUCTS ====================
    fun loadBestSellerProducts() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.getBestSellerProducts()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        val productList = body.data?.products ?: emptyList()
                        _products.value = productList
                        Log.d("HomeViewModel", "✅ Loaded ${productList.size} best seller products")
                    } else {
                        _errorMessage.value = body?.message ?: "Gagal memuat produk"
                        Log.e("HomeViewModel", "❌ API returned success=false")
                    }
                } else {
                    _errorMessage.value = "Gagal memuat produk: ${response.code()}"
                    Log.e("HomeViewModel", "❌ Response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal konek server: ${e.localizedMessage}"
                Log.e("HomeViewModel", "❌ Error loading products", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== LOAD CATEGORIES ====================
    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categoryList = withContext(Dispatchers.IO) {
                    CategoryRepository.getCategories()
                }

                _categories.value = categoryList
                Log.d("HomeViewModel", "✅ Loaded ${categoryList.size} categories")

                if (categoryList.isEmpty()) {
                    _errorMessage.value = "Belum ada kategori"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal load kategori: ${e.localizedMessage}"
                Log.e("HomeViewModel", "❌ Error loading categories", e)
            }
        }
    }

    // ==================== LOAD STORE INFO ====================
    fun loadStoreInfo() {
        viewModelScope.launch {
            try {
                val response = apiService.getSettings()

                if (response.isSuccessful && response.body()?.success == true) {
                    val settings = response.body()?.data as? Map<String, Any> ?: emptyMap()

                    val storeData = StoreInfoData(
                        appName = settings["app_name"]?.toString() ?: "Niaga Tani",
                        appTagline = settings["app_tagline"]?.toString() ?: "Solusi Pertanian Modern",
                        appLogo = settings["app_logo"]?.toString() ?: "",
                        appAddress = settings["app_address"]?.toString() ?: "Jl. Pertanian Sejahtera No. 123",
                        contactEmail = settings["contact_email"]?.toString() ?: "support@agrishop.com",
                        contactPhone = settings["contact_phone"]?.toString() ?: "081234567890"
                    )

                    _storeInfo.value = storeData
                    Log.d("HomeViewModel", "✅ Store info loaded")
                } else {
                    Log.e("HomeViewModel", "❌ Failed to load store info: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error loading store info", e)
                // Silent fail - not critical
            }
        }
    }

    // ==================== LOAD PROMOS ====================
    fun loadPromos() {
        viewModelScope.launch {
            try {
                val response = apiService.getActivePromos()

                if (response.isSuccessful && response.body()?.success == true) {
                    val promoList = response.body()?.data?.promos ?: emptyList()
                    _promos.value = promoList
                    Log.d("HomeViewModel", "✅ Loaded ${promoList.size} promos")
                } else {
                    Log.e("HomeViewModel", "❌ Failed to load promos: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error loading promos", e)
                // Silent fail - promo not critical
            }
        }
    }

    // ==================== UPLOAD PROMO ====================
    fun uploadPromo(token: String, imagePart: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val response = apiService.uploadPromo("Bearer $token", imagePart)

                if (response.isSuccessful && response.body()?.success == true) {
                    _successMessage.value = "Promo berhasil ditambahkan"
                    Log.d("HomeViewModel", "✅ Promo uploaded")
                    // Reload promos
                    loadPromos()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Gagal tambah promo: ${response.code()} $errorBody"
                    Log.e("HomeViewModel", "❌ Upload promo failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error upload"
                Log.e("HomeViewModel", "❌ Error uploading promo", e)
            }
        }
    }

    // ==================== DELETE PROMO ====================
    fun deletePromo(token: String, promoId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.deletePromo(
                    authHeader = "Bearer $token",
                    body = mapOf("id" to promoId)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _successMessage.value = "Promo berhasil dihapus"
                    Log.d("HomeViewModel", "✅ Promo deleted")
                    // Reload promos
                    loadPromos()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Gagal hapus promo: ${response.code()} $errorBody"
                    Log.e("HomeViewModel", "❌ Delete promo failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error delete"
                Log.e("HomeViewModel", "❌ Error deleting promo", e)
            }
        }
    }

    // ==================== CLEAR MESSAGES ====================
    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}

// ==================== DATA CLASS ====================
data class StoreInfoData(
    val appName: String,
    val appTagline: String,
    val appLogo: String,
    val appAddress: String,
    val contactEmail: String,
    val contactPhone: String
)