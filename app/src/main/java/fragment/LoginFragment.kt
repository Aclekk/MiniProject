package com.example.miniproject.fragment

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.MainActivity
import com.example.miniproject.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val dummyUsers = listOf(
        DummyUser(1, "budi", "123456", "user"),
        DummyUser(2, "admin", "123456", "admin"),
        DummyUser(3, "siti", "123456", "user")
    )

    data class DummyUser(val id: Int, val username: String, val password: String, val role: String)

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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .commit()
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

        val user = dummyUsers.find { it.username == username && it.password == password }

        if (user != null) {
            showLoading(false)
            saveUserData(user.id, user.username, user.role)

            Toast.makeText(
                requireContext(),
                "✅ Selamat datang, ${user.username.capitalize()}!",
                Toast.LENGTH_SHORT
            ).show()

            navigateToProducts(user.username, user.role)
        } else {
            showLoading(false)
            Toast.makeText(
                requireContext(),
                "❌ Username atau password salah!",
                Toast.LENGTH_SHORT
            ).show()
        }


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

    private fun navigateToProducts(username: String, role: String) {
        (requireActivity() as MainActivity).onLoginSuccess(username, role)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
