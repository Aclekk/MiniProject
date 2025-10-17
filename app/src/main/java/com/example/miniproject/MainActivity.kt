package com.example.miniproject

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.miniproject.fragment.ProductsFragment
import com.example.miniproject.fragment.HomeFragment
import com.example.miniproject.fragment.ProfileFragment
import com.example.miniproject.fragment.CategoriesFragment
import com.example.miniproject.fragment.CartFragment
import com.example.miniproject.fragment.LoginFragment
import com.example.miniproject.model.StoreProfile
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Cek apakah user sudah login
        if (isUserLoggedIn()) {
            showMainApp()
        } else {
            showLoginFragment()
        }

        // Listener navigasi bottom bar
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Home = ProductsFragment (tampilan promo/top selling)
                    open(ProductsFragment())
                    true
                }
                R.id.nav_products -> {
                    // Products = HomeFragment (katalog grid penuh)
                    open(HomeFragment())
                    true
                }
                R.id.nav_profile -> {
                    // Profil Pembeli / Admin
                    open(ProfileFragment())
                    true
                }
                R.id.nav_categories -> {
                    // Kategori produk
                    open(CategoriesFragment())
                    true
                }
                R.id.nav_cart -> {
                    // Keranjang
                    open(CartFragment())
                    true
                }
                else -> false
            }
        }

        // Jika ada intent dari notifikasi ke tab tertentu (misal: Cart)
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "cart") {
            bottomNavigation.selectedItemId = R.id.nav_cart
            open(CartFragment())
        }
    }

    // Fungsi buka fragment
    private fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    // Panggil ini untuk buka Category Page dengan filter kategori tertentu
    fun openCategoriesWithFilter(categoryId: Int?, categoryName: String?) {
        val fragment = CategoriesFragment().apply {
            arguments = Bundle().apply {
                if (categoryId != null) putInt("category_id", categoryId)
                if (!categoryName.isNullOrBlank()) putString("category_name", categoryName)
            }
        }
        // Pindah tab + buka fragment
        bottomNavigation.selectedItemId = R.id.nav_categories
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    // Saat user sudah login
    private fun showMainApp() {
        bottomNavigation.visibility = View.VISIBLE
        bottomNavigation.selectedItemId = R.id.nav_home
        open(ProductsFragment()) // default tampilan pertama
    }

    // Saat user belum login
    private fun showLoginFragment() {
        bottomNavigation.visibility = View.GONE
        open(LoginFragment())
    }

    // Simpan status login (dengan role)
    fun onLoginSuccess(username: String, role: String) {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", true)
            .putString("username", username)
            .putString("role", role)
            .apply()
        showMainApp()
    }

    // Logout
    fun onLogout() {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // ❌ JANGAN reset StoreProfile
        // StoreProfile tetap disimpan biar toko nggak hilang saat pindah role

        showLoginFragment()
    }


    // Cek apakah user sudah login
    private fun isUserLoggedIn(): Boolean {
        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sp.getBoolean("is_logged_in", false)
    }

    // Cek apakah user adalah admin
    fun isAdmin(): Boolean {
        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sp.getString("role", "user") == "admin"
    }

    // Get username yang sedang login
    fun getCurrentUsername(): String {
        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sp.getString("username", "User") ?: "User"
    }
}