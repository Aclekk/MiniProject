package com.example.miniproject.fragment

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.api.ApiClient
import com.example.miniproject.databinding.FragmentRegisterBinding
import com.example.miniproject.model.UserRequest
import com.example.miniproject.model.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener { registerUser() }

        binding.tvLoginLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun registerUser() {
        val username = binding.etRegisterUsername.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()
        val confirmPassword = binding.etRegisterConfirmPassword.text.toString().trim()

        if (!validateInput(username, email, password, confirmPassword)) return

        showLoading(true)

        val userRequest = UserRequest(
            username = username,
            password = password,
            email = email,
            role = "user" // default semua user baru jadi "user"
        )

        ApiClient.apiService.registerUser(userRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                showLoading(false)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(
                        requireContext(),
                        "Registration successful! Please login.",
                        Toast.LENGTH_LONG
                    ).show()

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment())
                        .commit()
                } else {
                    val errorMessage = response.body()?.message ?: "Registration failed"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        binding.etRegisterUsername.error = null
        binding.etRegisterEmail.error = null
        binding.etRegisterPassword.error = null
        binding.etRegisterConfirmPassword.error = null

        return when {
            username.isEmpty() -> {
                binding.etRegisterUsername.error = "Username is required"
                binding.etRegisterUsername.requestFocus()
                false
            }
            username.length < 3 -> {
                binding.etRegisterUsername.error = "Username must be at least 3 characters"
                binding.etRegisterUsername.requestFocus()
                false
            }
            email.isEmpty() -> {
                binding.etRegisterEmail.error = "Email is required"
                binding.etRegisterEmail.requestFocus()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etRegisterEmail.error = "Invalid email format"
                binding.etRegisterEmail.requestFocus()
                false
            }
            password.isEmpty() -> {
                binding.etRegisterPassword.error = "Password is required"
                binding.etRegisterPassword.requestFocus()
                false
            }
            password.length < 6 -> {
                binding.etRegisterPassword.error = "Password must be at least 6 characters"
                binding.etRegisterPassword.requestFocus()
                false
            }
            confirmPassword.isEmpty() -> {
                binding.etRegisterConfirmPassword.error = "Please confirm your password"
                binding.etRegisterConfirmPassword.requestFocus()
                false
            }
            password != confirmPassword -> {
                binding.etRegisterConfirmPassword.error = "Passwords do not match"
                binding.etRegisterConfirmPassword.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBarRegister.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
        binding.tvLoginLink.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
