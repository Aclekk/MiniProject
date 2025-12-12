// File: UserManager.kt (di root package atau utils)
package com.example.miniproject

import android.content.Context
import android.content.SharedPreferences
import com.example.miniproject.data.model.User
import com.google.gson.Gson

class UserManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("AgriToolsPrefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "user_data"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        @Volatile
        private var instance: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // ========== Save User Data ==========
    fun saveUser(user: User, token: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER, gson.toJson(user))
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // ========== Get Token ==========
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // ========== Get Token with Bearer ==========
    fun getAuthToken(): String? {
        val token = getToken()
        return if (token != null) "Bearer $token" else null
    }

    // ========== Get User Data ==========
    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    // ========== Check if Logged In ==========
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null
    }

    // ========== Get User Role ==========
    fun getUserRole(): String? {
        return getUser()?.role
    }

    // ========== Check if Admin/Seller ==========
    fun isAdmin(): Boolean {
        return getUserRole() == "admin"
    }

    fun isSeller(): Boolean {
        val role = getUserRole()
        return role == "seller" || role == "admin"
    }

    fun isBuyer(): Boolean {
        return getUserRole() == "buyer"
    }

    // ========== Update User Data ==========
    fun updateUser(user: User) {
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()
    }

    // ========== Logout ==========
    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        // balikin ke login
    }

    // ========== Clear Data ==========
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}