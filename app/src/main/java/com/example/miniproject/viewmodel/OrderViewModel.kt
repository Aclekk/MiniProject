package com.example.miniproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.Order
import com.example.miniproject.data.api.ApiService
import com.example.miniproject.data.mapper.toOrderModel
import kotlinx.coroutines.launch

/**
 * ViewModel untuk Order Management
 * SHARED by: ActiveOrdersFragment (buyer) & AdminOrderListFragment (seller)
 */
class OrderViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // ===== BUYER ORDERS =====
    private val _buyerOrders = MutableLiveData<List<Order>>()
    val buyerOrders: LiveData<List<Order>> = _buyerOrders

    // ===== SELLER ORDERS =====
    private val _sellerOrders = MutableLiveData<List<Order>>()
    val sellerOrders: LiveData<List<Order>> = _sellerOrders

    // ===== UI STATE =====
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // ==================== BUYER: FETCH ORDERS ====================
    fun fetchBuyerOrders(token: String, status: String? = null) {
        viewModelScope.launch {
            try {
                val response = apiService.getBuyerOrders(
                    token = "Bearer $token",
                    status = status
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val mapped = body.data.map { it.toOrderModel() }
                        _buyerOrders.value = mapped
                        Log.d("OrderViewModel", "✅ Loaded ${mapped.size} buyer orders")
                    } else {
                        _errorMessage.value = body?.message ?: "Gagal memuat pesanan buyer"
                        Log.e("OrderViewModel", "❌ Buyer orders load failed")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "HTTP ${response.code()}: ${errorBody ?: "error"}"
                    Log.e("OrderViewModel", "❌ Response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
                Log.e("OrderViewModel", "❌ Error fetching buyer orders", e)
            }
        }
    }

    // ==================== SELLER: FETCH ORDERS ====================
    fun fetchSellerOrders(token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getSellerOrders(token = "Bearer $token")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val mapped = body.data.map { it.toOrderModel() }
                        _sellerOrders.value = mapped
                        Log.d("OrderViewModel", "✅ Loaded ${mapped.size} seller orders")
                    } else {
                        _errorMessage.value = body?.message ?: "Gagal memuat pesanan"
                        Log.e("OrderViewModel", "❌ Seller orders load failed")
                    }
                } else {
                    _errorMessage.value = "HTTP ${response.code()}"
                    Log.e("OrderViewModel", "❌ Response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
                Log.e("OrderViewModel", "❌ Error fetching seller orders", e)
            }
        }
    }

    // ==================== SELLER: UPDATE ORDER STATUS ====================
    fun updateOrderStatus(
        token: String,
        orderId: Int,
        nextStatus: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val payload = mapOf(
                    "order_id" to orderId.toString(),
                    "status" to nextStatus
                )

                val response = apiService.updateOrderStatus(
                    token = "Bearer $token",
                    body = payload
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _successMessage.value = "Status berhasil diupdate!"
                    Log.d("OrderViewModel", "✅ Order status updated to $nextStatus")
                    onSuccess()
                } else {
                    _errorMessage.value = "Gagal update status"
                    Log.e("OrderViewModel", "❌ Status update failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
                Log.e("OrderViewModel", "❌ Error updating status", e)
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