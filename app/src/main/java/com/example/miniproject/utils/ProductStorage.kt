package com.example.miniproject.utils

import android.content.Context
import com.example.miniproject.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ProductStorage {

    private const val PREF_NAME = "product_pref"
    private const val KEY_PRODUCTS = "products"

    fun saveProducts(context: Context, products: List<Product>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(products)
        prefs.edit().putString(KEY_PRODUCTS, json).apply()
    }

    fun loadProducts(context: Context): MutableList<Product> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PRODUCTS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Product>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // ✅ Tambahkan ini biar bisa reset data
    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PRODUCTS).apply()
    }
}
