package com.example.miniproject

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.miniproject.fragment.LoginFragment
import com.example.miniproject.fragment.ProductsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if user is already logged in
        if (savedInstanceState == null) {
            if (isUserLoggedIn()) {
                // Show products fragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProductsFragment())
                    .commit()
            } else {
                // Show login fragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
            }
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("is_logged_in", false)
    }
}