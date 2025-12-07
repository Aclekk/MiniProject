// File: data/viewmodel/ProfileViewModel.kt
package com.example.miniproject.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.ProfileData
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProfileViewModel : ViewModel() {

    private val apiService = ApiClient.apiService

    // LiveData for profile
    private val _profileData = MutableLiveData<ProfileData?>()
    val profileData: LiveData<ProfileData?> = _profileData

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Success message
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    /**
     * Fetch Profile from API
     */
    fun fetchProfile(token: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = apiService.getProfile("Bearer $token")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        _profileData.value = body.data
                    } else {
                        _errorMessage.value = body?.message ?: "Failed to load profile"
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Connection failed: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update Profile
     */
    fun updateProfile(
        token: String,
        fullName: String?,
        email: String?,
        phone: String?,
        address: String?,
        city: String?,
        province: String?,
        postalCode: String?,
        imageFile: File?
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                // Prepare multipart data - only add if not null
                val fullNameBody = fullName?.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val emailBody = email?.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val phoneBody = phone?.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val addressBody = address?.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val cityBody = city?.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val provinceBody = province?.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())
                val postalCodeBody = postalCode?.takeIf { it.isNotEmpty() }
                    ?.toRequestBody("text/plain".toMediaTypeOrNull())

                // Prepare image part
                val imagePart = if (imageFile != null && imageFile.exists()) {
                    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("profile_image", imageFile.name, requestFile)
                } else {
                    null
                }

                val response = apiService.updateProfile(
                    token = "Bearer $token",
                    fullName = fullNameBody,
                    email = emailBody,
                    phone = phoneBody,
                    address = addressBody,
                    city = cityBody,
                    province = provinceBody,
                    postalCode = postalCodeBody,
                    recipientName = fullNameBody,  // ✅ TAMBAHAN
                    recipientPhone = phoneBody,  // ✅ TAMBAHAN
                    profileImage = imagePart
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        _profileData.value = body.data
                        _successMessage.value = "Profile updated successfully!"
                    } else {
                        _errorMessage.value = body?.message ?: "Failed to update profile"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = parseErrorMessage(errorBody) ?: "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Connection failed: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Parse error message dari JSON
     */
    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody == null) return null

        return try {
            if (errorBody.contains("message")) {
                val start = errorBody.indexOf("\"message\":\"") + 11
                val end = errorBody.indexOf("\"", start)
                if (start > 0 && end > start) {
                    errorBody.substring(start, end)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clear messages
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}