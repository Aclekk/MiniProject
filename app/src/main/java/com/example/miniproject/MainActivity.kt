package com.example.miniproject

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.miniproject.fragment.CartFragment
import com.example.miniproject.fragment.CategoriesFragment
import com.example.miniproject.fragment.HomeFragment       // ⬅️ pastikan file HomeFragment punya package yg benar
import com.example.miniproject.fragment.LoginFragment
import com.example.miniproject.fragment.ProductsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Tentukan tampilan awal
        if (isUserLoggedIn()) {
            showMainApp()
        } else {
            showLoginFragment()
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {                     // Home = halaman “promo + top selling” (ProductsFragment lama)
                    open(ProductsFragment())
                    true
                }
                R.id.nav_products -> {                 // Products = katalog grid penuh (HomeFragment baru)
                    open(HomeFragment())
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
                else -> false
            }
        }
    }

    private fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    private fun showMainApp() {
        bottomNavigation.visibility = View.VISIBLE
        // Default tab → Home
        bottomNavigation.selectedItemId = R.id.nav_home
        open(ProductsFragment())
    }

    private fun showLoginFragment() {
        bottomNavigation.visibility = View.GONE
        open(LoginFragment())
    }

    fun onLoginSuccess() {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit().putBoolean("is_logged_in", true).apply()
        showMainApp()
    }

    fun onLogout() {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit().clear().apply()
        showLoginFragment()
    }

    private fun isUserLoggedIn(): Boolean {
        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sp.getBoolean("is_logged_in", false)
    }
}
