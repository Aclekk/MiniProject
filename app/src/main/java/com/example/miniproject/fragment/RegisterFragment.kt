package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // DUMMY USERS - simulasi data user terdaftar
    private val registeredUsers = mutableListOf(
        DummyUser(1, "budi", "budi@gmail.com", "123456", "user"),
        DummyUser(2, "admin", "admin@gmail.com", "123456", "admin"),
        DummyUser(3, "siti", "siti@gmail.com", "123456", "user")
    )

    data class DummyUser(
        val id: Int,
        val username: String,
        val email: String,
        val password: String,
        val role: String
    )

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
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(username, password, confirmPassword)) {
                performRegister(username, password)
            }
        }

        binding.tvLoginLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun validateInput(username: String, password: String, confirmPassword: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.etUsername.error = "Username required"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password required"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                false
            }
            confirmPassword != password -> {
                binding.etConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    private fun performRegister(username: String, password: String) {
        showLoading(true)

        val usernameExists = registeredUsers.any { it.username == username }
        val email = "$username@gmail.com" // auto generate dummy email
        val emailExists = registeredUsers.any { it.email == email }

        if (usernameExists) {
            showLoading(false)
            Toast.makeText(requireContext(), "❌ Username sudah digunakan!", Toast.LENGTH_SHORT).show()
            return
        }

        if (emailExists) {
            showLoading(false)
            Toast.makeText(requireContext(), "❌ Email sudah terdaftar!", Toast.LENGTH_SHORT).show()
            return
        }

        val newId = (registeredUsers.maxOfOrNull { it.id } ?: 0) + 1
        registeredUsers.add(DummyUser(newId, username, email, password, "user"))

        showLoading(false)
        Toast.makeText(requireContext(), "✅ Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show()

        // Arahkan ke LoginFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBarRegister.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
