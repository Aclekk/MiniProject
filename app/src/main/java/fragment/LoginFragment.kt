package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.MainActivity
import com.example.miniproject.R
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.LoginRequest
import com.example.miniproject.data.model.User
import com.example.miniproject.databinding.FragmentLoginBinding
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

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

        val request = LoginRequest(
            username = username,
            password = password
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.login(request)

                if (response.isSuccessful) {
                    val body = response.body()   // BaseResponse<LoginResponse>

                    if (body?.success == true && body.data != null) {
                        val loginData = body.data      // LoginResponse
                        val user = loginData.user      // User
                        val token = loginData.token    // <-- INI yang dipakai


                        // ✅ SIMPAN SEMUA DATA USER
                        saveCompleteUserData(user, token)

                        // ✅ SYNC FCM TOKEN SETELAH LOGIN (BUYER & SELLER)
                        syncFcmTokenWithServer(token)

                        Toast.makeText(
                            requireContext(),
                            "✅ Selamat datang, ${user.fullName}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        navigateToProducts(user.username ?: user.fullName, user.role)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Username atau password salah!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorText = response.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        errorText ?: "Username atau password salah!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek ke server: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }

    // ✅ Simpan semua data user ke SharedPreferences
    private fun saveCompleteUserData(user: User, token: String) {
        val sharedPref =
            requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("user_id", user.id)
            putString("username", user.username ?: "")
            putString("full_name", user.fullName)
            putString("email", user.email)
            putString("phone", user.phone ?: "")
            putString("role", user.role)
            putString("profile_image", user.profileImage ?: "")
            putString("token", token)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    // ✅ Sinkronkan FCM token ke backend SETIAP kali login
    // ✅ Sinkronkan FCM token ke backend SETIAP kali login
    private fun syncFcmTokenWithServer(authToken: String) {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val localFcm = prefs.getString("fcm_token", null)

        fun sendToServer(fcm: String) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val body = mapOf("fcm_token" to fcm)
                    ApiClient.apiService.updateFcmToken(
                        "Bearer $authToken",
                        body
                    )
                } catch (_: Exception) {
                    // kalau gagal ya di-skip, bukan feature utama
                }
            }
        }

        // Kalau sudah punya FCM token di prefs → kirim ke server
        if (!localFcm.isNullOrEmpty()) {
            sendToServer(localFcm)
            return
        }

        // Kalau belum ada → minta token baru ke Firebase
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { fcm ->
                prefs.edit().putString("fcm_token", fcm).apply()
                sendToServer(fcm)
            }
            .addOnFailureListener {
                // kalau gagal, ya sudah, bukan kiamat
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
