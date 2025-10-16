package com.example.miniproject

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.miniproject.databinding.ActivityMainBinding
import com.example.miniproject.fragment.CartFragmentUser
import com.example.miniproject.fragment.HomeFragment
import com.example.miniproject.fragment.LoginFragment
import com.example.miniproject.fragment.ProductsFragment
import com.example.miniproject.fragment.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is logged in
        if (!isUserLoggedIn()) {
            showLoginFragment()
        } else {
            // Tampilkan fragment awal
            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
            }
            setupBottomNavigation()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("is_logged_in", false)
    }

    private fun showLoginFragment() {
        binding.bottomNavigation.visibility = android.view.View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    // ✅ Method yang dipanggil dari LoginFragment
    fun onLoginSuccess() {
        binding.bottomNavigation.visibility = android.view.View.VISIBLE
        replaceFragment(HomeFragment())
        setupBottomNavigation()
    }

    // 🔹 Navigasi bawah (BottomNavigationView)
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_products -> {
                    replaceFragment(ProductsFragment())
                    true
                }
                R.id.nav_cart -> {
                    // ✅ Gunakan CartFragmentUser untuk tampilan user
                    replaceFragment(CartFragmentUser())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    // 🔹 Fungsi ganti fragment
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}