package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.api.ApiClient
import com.example.miniproject.model.AuthResponse
import com.example.miniproject.model.RegisterRequest
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
    }

    private fun initViews(view: View) {
        etUsername = view.findViewById(R.id.etRegisterUsername)
        etEmail = view.findViewById(R.id.etRegisterEmail)
        etPassword = view.findViewById(R.id.etRegisterPassword)
        etConfirmPassword = view.findViewById(R.id.etRegisterConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvLoginLink = view.findViewById(R.id.tvLoginLink)
        progressBar = view.findViewById(R.id.progressBarRegister)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(username, email, password, confirmPassword)) {
                performRegister(username, email, password)
            }
        }

        tvLoginLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            username.isEmpty() -> {
                etUsername.error = "Username required"
                false
            }
            email.isEmpty() -> {
                etEmail.error = "Email required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                etPassword.error = "Password required"
                false
            }
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                false
            }
            confirmPassword != password -> {
                etConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    private fun performRegister(username: String, email: String, password: String) {
        showLoading(true)

        val registerRequest = RegisterRequest(
            username = username,
            password = password,
            email = email,
            role = "user"
        )

        ApiClient.apiService.register(registerRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        Toast.makeText(requireContext(), "Registration successful! Please login", Toast.LENGTH_LONG).show()

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, LoginFragment())
                            .commit()
                    } else {
                        Toast.makeText(requireContext(), authResponse?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Registration failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !show
    }
}