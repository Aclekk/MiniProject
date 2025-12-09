package com.example.miniproject

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.miniproject.data.model.Product
import com.example.miniproject.fragment.ProductsFragment
import com.example.miniproject.fragment.HomeFragment
import com.example.miniproject.fragment.ProfileFragment
import com.example.miniproject.fragment.CategoriesFragment
import com.example.miniproject.fragment.CartFragment
import com.example.miniproject.fragment.LoginFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Cek login
        if (isUserLoggedIn()) {
            showMainApp()
        } else {
            showLoginFragment()
        }

        // Bottom nav - DIPERBAIKI
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    open(ProductsFragment())  // ✅ HOME = ProductsFragment
                    true
                }
                R.id.nav_products -> {
                    open(HomeFragment())      // ✅ PRODUCTS = HomeFragment
                    true
                }
                R.id.nav_profile -> {
                    open(ProfileFragment())
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

        // Kalau ada intent ke cart
        val navigateTo = intent.getStringExtra("navigate_to")
        if (navigateTo == "cart") {
            bottomNavigation.selectedItemId = R.id.nav_cart
            open(CartFragment())
        }
    }

    private fun open(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    fun openCategoriesWithFilter(categoryId: Int?, categoryName: String?) {
        val fragment = CategoriesFragment().apply {
            arguments = Bundle().apply {
                if (categoryId != null) putInt("category_id", categoryId)
                if (!categoryName.isNullOrBlank()) putString("category_name", categoryName)
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_categories
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setReorderingAllowed(true)
            .commit()
    }

    // User sudah login
    private fun showMainApp() {
        bottomNavigation.visibility = View.VISIBLE
        // default: Home (yang sekarang ProductsFragment)
        bottomNavigation.selectedItemId = R.id.nav_home
        open(ProductsFragment())  // ✅ Default = ProductsFragment
    }

    // Belum login
    private fun showLoginFragment() {
        bottomNavigation.visibility = View.GONE
        open(LoginFragment())
    }

    // Dipanggil dari LoginFragment
    fun onLoginSuccess(username: String, role: String) {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", true)
            .putString("username", username)
            .putString("role", role)   // "buyer" / "seller"
            .apply()

        showMainApp()
    }

    fun onLogout() {
        getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        showLoginFragment()
    }

    private fun isUserLoggedIn(): Boolean {
            val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            return sp.getBoolean("is_logged_in", false)
    }

    fun isAdmin(): Boolean {
        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val role = sp.getString("role", "buyer")
        return role == "seller"
    }

    fun getCurrentUsername(): String {
        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sp.getString("username", "User") ?: "User"
    }
}