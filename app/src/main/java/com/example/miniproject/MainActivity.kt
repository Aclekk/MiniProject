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

        // ✅ Cek apakah user sudah login
        if (isUserLoggedIn()) {
            showMainApp()
        } else {
            showLoginFragment()
        }

        // ✅ Bottom navigation listener
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    open(ProductsFragment()) // tampilan utama (produk + promo)
                    true
                }
                R.id.nav_products -> {
                    open(HomeFragment()) // katalog produk penuh
                    true
                }
                R.id.nav_categories -> {
                    open(CategoriesFragment())
                    true
                }
                R.id.nav_cart -> {
                    open(CartFragment())
                    true
                }
                R.id.nav_profile -> {
                    val role = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                        .getString("role", "user")
                    if (role == "admin") {
                        open(StoreProfileFragment()) // langsung ke profil toko
                    } else {
                        open(ProfileFragment()) // profil pembeli
                    }
                    true
                }
                else -> false
            }
        }

        // ✅ Navigasi dari notifikasi ke tab tertentu
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "cart") {
            bottomNavigation.selectedItemId = R.id.nav_cart
            open(CartFragment())
        }
    }

    // ✅ Fungsi umum untuk membuka fragment
    private fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commitAllowingStateLoss() // lebih aman saat config change
    }

    // ✅ Saat user sudah login
    private fun showMainApp() {
        bottomNavigation.visibility = View.VISIBLE

        val sharedPref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val role = sharedPref.getString("role", "user")

        // kalau admin → langsung buka profil toko
        if (role == "admin") {
            bottomNavigation.selectedItemId = R.id.nav_profile
            open(StoreProfileFragment())
        } else {
            bottomNavigation.selectedItemId = R.id.nav_home
            open(ProductsFragment())
        }
    }

    // ✅ Saat user belum login
    private fun showLoginFragment() {
        bottomNavigation.visibility = View.GONE
        open(LoginFragment())
    }

    // ✅ Saat login sukses
    fun onLoginSuccess() {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", true)
            .apply()
        showMainApp()
    }

    // ✅ Logout user
    fun onLogout() {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        showLoginFragment()
    }

    // ✅ Cek apakah user sudah login
    private fun isUserLoggedIn(): Boolean {
        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sp.getBoolean("is_logged_in", false)
    }
}
