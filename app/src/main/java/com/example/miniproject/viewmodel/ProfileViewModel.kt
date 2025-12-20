package com.example.miniproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.api.ApiService
import com.example.miniproject.data.model.ProfileData
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

/**
 * ViewModel untuk ProfileFragment
 * Handles both BUYER and SELLER profile operations
 */
class ProfileViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // ===== PROFILE DATA (BUYER) =====
    private val _profileData = MutableLiveData<ProfileData>()
    val profileData: LiveData<ProfileData> = _profileData

    // ===== STORE SETTINGS (SELLER) =====
    private val _storeSettings = MutableLiveData<Map<String, Any>>()
    val storeSettings: LiveData<Map<String, Any>> = _storeSettings

    // ===== UI STATE =====
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    // ==================== BUYER: LOAD PROFILE ====================
    fun loadUserProfile(token: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.getProfile("Bearer $token")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        _profileData.value = body.data
                        Log.d("ProfileViewModel", "✅ User profile loaded")
                    } else {
                        _errorMessage.value = body?.message ?: "Gagal memuat profil"
                        Log.e("ProfileViewModel", "❌ API returned success=false")
                    }
                } else {
                    _errorMessage.value = "Error ${response.code()}"
                    Log.e("ProfileViewModel", "❌ Response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal konek: ${e.localizedMessage}"
                Log.e("ProfileViewModel", "❌ Error loading profile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== BUYER: UPDATE PROFILE ====================
    fun updateBuyerProfile(
        token: String,
        fullName: RequestBody,
        email: RequestBody,
        phone: RequestBody?,
        address: RequestBody?,
        profileImage: MultipartBody.Part?
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.updateProfile(
                    token = "Bearer $token",
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    address = address,
                    city = null,
                    province = null,
                    postalCode = null,
                    recipientName = null,
                    recipientPhone = null,
                    profileImage = profileImage
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        _profileData.value = body.data
                        _successMessage.value = "✅ Profil berhasil diperbarui!"
                        Log.d("ProfileViewModel", "✅ Profile updated")
                    } else {
                        _errorMessage.value = body?.message ?: "Gagal update profil"
                        Log.e("ProfileViewModel", "❌ Update failed")
                    }
                } else {
                    _errorMessage.value = "Error ${response.code()}"
                    Log.e("ProfileViewModel", "❌ Response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal konek: ${e.localizedMessage}"
                Log.e("ProfileViewModel", "❌ Error updating profile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== SELLER: LOAD STORE SETTINGS ====================
    fun loadStoreSettings() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.getSettings()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        @Suppress("UNCHECKED_CAST")
                        _storeSettings.value = body.data as Map<String, Any>
                        Log.d("ProfileViewModel", "✅ Store settings loaded")
                    } else {
                        _errorMessage.value = "Gagal memuat settings toko"
                        Log.e("ProfileViewModel", "❌ Settings load failed")
                    }
                } else {
                    _errorMessage.value = "Error ${response.code()}"
                    Log.e("ProfileViewModel", "❌ Response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal konek: ${e.localizedMessage}"
                Log.e("ProfileViewModel", "❌ Error loading settings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== SELLER: UPLOAD LOGO ====================
    fun uploadStoreLogo(token: String, logo: MultipartBody.Part, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.uploadStoreLogo(
                    token = "Bearer $token",
                    logo = logo
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _successMessage.value = "Logo berhasil diupload"
                    Log.d("ProfileViewModel", "✅ Logo uploaded")
                    onSuccess()
                } else {
                    _errorMessage.value = "Gagal upload logo (${response.code()})"
                    Log.e("ProfileViewModel", "❌ Logo upload failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error upload: ${e.localizedMessage}"
                Log.e("ProfileViewModel", "❌ Error uploading logo", e)
            }
        }
    }

    // ==================== SELLER: UPDATE STORE SETTINGS ====================
    fun updateStoreSettings(
        token: String,
        appName: String,
        contactEmail: String,
        contactPhone: String,
        appTagline: String,
        contactWhatsapp: String,
        appAddress: String
    ) {
        viewModelScope.launch {
            try {
                val requestBody = mapOf(
                    "app_name" to appName,
                    "contact_email" to contactEmail,
                    "contact_phone" to contactPhone,
                    "app_tagline" to appTagline,
                    "contact_whatsapp" to contactWhatsapp,
                    "app_address" to appAddress
                )

                val response = apiService.updateSettings(
                    token = "Bearer $token",
                    request = requestBody
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _successMessage.value = "✅ Profil toko berhasil diperbarui!"
                    Log.d("ProfileViewModel", "✅ Store settings updated")
                    // Reload settings after update
                    loadStoreSettings()
                } else {
                    _errorMessage.value = response.body()?.message ?: "Gagal update settings"
                    Log.e("ProfileViewModel", "❌ Settings update failed")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Gagal konek: ${e.localizedMessage}"
                Log.e("ProfileViewModel", "❌ Error updating settings", e)
            } finally {
                _isLoading.value = false
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