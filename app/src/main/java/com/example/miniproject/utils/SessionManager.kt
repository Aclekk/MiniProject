package com.example.miniproject.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", 0)
    }

    fun getRole(): String? {
        return prefs.getString("role", null)
    }
}