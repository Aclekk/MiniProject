package com.example.miniproject.data

import android.content.Context
import android.util.Log
import com.example.miniproject.R
import com.example.miniproject.data.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

// Alias untuk membedakan model API & UI
import com.example.miniproject.data.model.Category as ApiCategory
import com.example.miniproject.model.Category as UiCategory

object CategoryRepository {

    private const val TAG = "CategoryRepository"

    // Dulu dipakai untuk DatabaseHelper, sekarang dibiarkan supaya
    // tidak merusak kode lain yang masih memanggil initialize()
    fun initialize(context: Context) {
        // no-op
    }

    // =========================================================
    //  GET CATEGORIES  (dipanggil sinkron dari banyak tempat)
    // =========================================================
    fun getCategories(): List<UiCategory> {
        return try {
            runBlocking(Dispatchers.IO) {
                val response = ApiClient.apiService.getCategories()

                if (!response.isSuccessful) {
                    Log.e(TAG, "getCategories() HTTP error: ${response.code()}")
                    Log.e(TAG, "errorBody: ${response.errorBody()?.string()}")
                    return@runBlocking emptyList()
                }

                val body = response.body()
                if (body == null) {
                    Log.e(TAG, "getCategories() body null")
                    return@runBlocking emptyList()
                }

                if (body.success != true) {
                    Log.e(TAG, "getCategories() success=false, message=${body.message}")
                    return@runBlocking emptyList()
                }

                val apiCategories: List<ApiCategory> =
                    body.data?.categories ?: emptyList()

                apiCategories.map { apiCat ->
                    UiCategory(
                        id = apiCat.id,
                        categoryName = apiCat.name,
                        createdAt = apiCat.createdAt ?: "",
                        icon = R.drawable.ic_category
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories() exception", e)
            emptyList()
        }
    }

    // =========================================================
    //  ADD CATEGORY
    // =========================================================
    fun addCategory(name: String, iconResId: Int): Boolean {
        return try {
            runBlocking(Dispatchers.IO) {
                val resp = ApiClient.apiService.addCategory(
                    name = name,
                    description = "Kategori $name"
                )

                if (!resp.isSuccessful) {
                    Log.e(TAG, "addCategory() HTTP error: ${resp.code()}")
                    Log.e(TAG, "errorBody: ${resp.errorBody()?.string()}")
                    return@runBlocking false
                }

                val body = resp.body()
                if (body == null) {
                    Log.e(TAG, "addCategory() body null")
                    return@runBlocking false
                }

                if (body.success != true) {
                    Log.e(TAG, "addCategory() success=false, message=${body.message}")
                    // kalau mau lihat error detail dari PHP:
                    Log.e(TAG, "addCategory() errors=${body.errors}")
                    return@runBlocking false
                }

                Log.d(TAG, "addCategory() success: ${body.message}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "addCategory() exception", e)
            false
        }
    }

    // =========================================================
    //  UPDATE CATEGORY
    // =========================================================
    fun updateCategory(id: Int, name: String): Boolean {
        return try {
            runBlocking(Dispatchers.IO) {
                val resp = ApiClient.apiService.updateCategory(
                    id = id,
                    name = name,
                    description = "Kategori $name"
                )

                if (!resp.isSuccessful) {
                    Log.e(TAG, "updateCategory() HTTP error: ${resp.code()}")
                    Log.e(TAG, "errorBody: ${resp.errorBody()?.string()}")
                    return@runBlocking false
                }

                val body = resp.body()
                if (body == null) {
                    Log.e(TAG, "updateCategory() body null")
                    return@runBlocking false
                }

                if (body.success != true) {
                    Log.e(TAG, "updateCategory() success=false, message=${body.message}")
                    Log.e(TAG, "updateCategory() errors=${body.errors}")
                    return@runBlocking false
                }

                Log.d(TAG, "updateCategory() success: ${body.message}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateCategory() exception", e)
            false
        }
    }


    // =========================================================
    //  DELETE CATEGORY
    // =========================================================
    fun deleteCategory(id: Int): Boolean {
        return try {
            runBlocking(Dispatchers.IO) {
                val resp = ApiClient.apiService.deleteCategory(id)

                if (!resp.isSuccessful) {
                    Log.e(TAG, "deleteCategory() HTTP error: ${resp.code()}")
                    Log.e(TAG, "errorBody: ${resp.errorBody()?.string()}")
                    return@runBlocking false
                }

                val body = resp.body()
                if (body == null) {
                    Log.e(TAG, "deleteCategory() body null")
                    return@runBlocking false
                }

                if (body.success != true) {
                    Log.e(TAG, "deleteCategory() success=false, message=${body.message}")
                    Log.e(TAG, "deleteCategory() errors=${body.errors}")
                    return@runBlocking false
                }

                Log.d(TAG, "deleteCategory() success: ${body.message}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteCategory() exception", e)
            false
        }
    }

    // =========================================================
    //  CEK NAMA KATEGORI SUDAH ADA ATAU BELUM
    // =========================================================
    fun isCategoryNameExists(name: String): Boolean {
        val all = getCategories()
        val exists = all.any { it.categoryName.equals(name, ignoreCase = true) }
        Log.d(TAG, "isCategoryNameExists('$name') = $exists")
        return exists
    }
}
