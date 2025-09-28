package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.api.ApiClient
import com.example.miniproject.databinding.FragmentLoginBinding
import com.example.miniproject.model.AuthResponse
import com.example.miniproject.model.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.miniproject.MainActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(username, password)) {
                performLogin(username, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            // TODO: Navigate to register fragment (for now just show toast)
            Toast.makeText(requireContext(), "Register feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.etUsername.error = "Username required"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password required"
                false
            }
            else -> true
        }
    }

    private fun performLogin(username: String, password: String) {
        showLoading(true)

        val loginRequest = LoginRequest(username, password)
        ApiClient.apiService.login(loginRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true && authResponse.user != null) {
                        // Save user data to SharedPreferences
                        saveUserData(authResponse.user!!.id, authResponse.user!!.username, authResponse.user!!.role)

                        // Navigate to products fragment
                        navigateToProducts()

                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), authResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }

    private fun saveUserData(userId: Int, username: String, role: String) {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("user_id", userId)
            putString("username", username)
            putString("role", role)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    private fun navigateToProducts() {
        (requireActivity() as MainActivity).onLoginSuccess()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}