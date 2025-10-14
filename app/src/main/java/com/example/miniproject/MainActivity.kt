package com.example.miniproject

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.miniproject.fragment.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // 🔹 Cek apakah user sudah login
        if (isUserLoggedIn()) {
            showMainApp()
        } else {
            showLoginFragment()
        }

        // 🔹 Listener navigasi bottom bar
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // 🏠 Home = ProductsFragment (tampilan promo/top selling)
                    open(ProductsFragment())
                    true
                }
                R.id.nav_products -> {
                    // 📦 Products = HomeFragment (katalog grid penuh)
                    open(HomeFragment())
                    true
                }
                R.id.nav_profile -> {
                    // 👤 Profil Pembeli
                    open(ProfileFragment())
                    true
                }
                R.id.nav_categories -> {
                    // 📂 Kategori produk
                    open(CategoriesFragment())
                    true
                }
                R.id.nav_cart -> {
                    // 🛒 Keranjang
                    open(CartFragment())
                    true
                }
                else -> false
            }
        }

        // 🔹 Jika ada intent dari notifikasi ke tab tertentu (misal: Cart)
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "cart") {
            bottomNavigation.selectedItemId = R.id.nav_cart
            open(CartFragment())
        }
    }

    // 🔹 Fungsi buka fragment
    private fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    // 🔹 Saat user sudah login
    private fun showMainApp() {
        bottomNavigation.visibility = View.VISIBLE
        bottomNavigation.selectedItemId = R.id.nav_home
        open(ProductsFragment()) // default tampilan pertama
    }

    // 🔹 Saat user belum login
    private fun showLoginFragment() {
        bottomNavigation.visibility = View.GONE
        open(LoginFragment())
    }

    // 🔹 Simpan status login
    fun onLoginSuccess() {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", true)
            .apply()
        showMainApp()
    }

    // 🔹 Logout
    fun onLogout() {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        showLoginFragment()
    }

    // 🔹 Cek apakah user sudah login
    private fun isUserLoggedIn(): Boolean {
        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sp.getBoolean("is_logged_in", false)
    }
}
