package com.example.miniproject

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.miniproject.fragment.LoginFragment
import com.example.miniproject.fragment.ProductsFragment
import com.example.miniproject.fragment.CategoriesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Check if user is already logged in
        if (savedInstanceState == null) {
            if (isUserLoggedIn()) {
                showMainApp()
            } else {
                showLoginFragment()
            }
        }
    }

    fun showMainApp() {
        // Show bottom navigation
        bottomNavigation.visibility = android.view.View.VISIBLE

        // Show default fragment (Products)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProductsFragment())
            .commit()

        // Setup bottom navigation listener
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_products -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ProductsFragment())
                        .commit()
                    true
                }
                R.id.nav_categories -> {
                    // Always create new instance untuk fresh data
                    val categoriesFragment = CategoriesFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, categoriesFragment)
                        .commit()
                    true
                }
                else -> false
            }
        }
    } // <--- ini yang tadi kurang

    fun showLoginFragment() {
        // Hide bottom navigation
        bottomNavigation.visibility = android.view.View.GONE

        // Show login fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    fun onLoginSuccess() {
        showMainApp()
    }

    fun onLogout() {
        showLoginFragment()
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("is_logged_in", false)
    }
}
