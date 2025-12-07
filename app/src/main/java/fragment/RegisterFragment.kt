package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.R
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.RegisterRequest
import com.example.miniproject.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

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
        binding.btnRegister.setOnClickListener {
            // ambil semua input & trim
            val username       = binding.etUsername.text.toString().trim()
            val fullName       = binding.etFullName.text.toString().trim()
            val email          = binding.etEmail.text.toString().trim()
            val phone          = binding.etPhone.text.toString().trim()
            val password       = binding.etPassword.text.toString().trim()
            val confirmPassword= binding.etConfirmPassword.text.toString().trim()

            val address        = binding.etAddress.text.toString().trim()
            val city           = binding.etCity.text.toString().trim()
            val province       = binding.etProvince.text.toString().trim()
            val postalCode     = binding.etPostalCode.text.toString().trim()

            if (
                validateInput(
                    username = username,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    address = address,
                    city = city,
                    province = province,
                    postalCode = postalCode,
                    password = password,
                    confirmPassword = confirmPassword
                )
            ) {
                performRegister(
                    username = username,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    password = password,
                    address = address,
                    city = city,
                    province = province,
                    postalCode = postalCode
                )
            }
        }

        binding.tvLoginLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

    private fun validateInput(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        address: String,
        city: String,
        province: String,
        postalCode: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // bisa ditambah clear error kalau mau
        return when {
            username.isEmpty() -> {
                binding.etUsername.error = "Username wajib diisi"
                false
            }
            username.length < 3 -> {
                binding.etUsername.error = "Username minimal 3 karakter"
                false
            }
            fullName.isEmpty() -> {
                binding.etFullName.error = "Nama wajib diisi"
                false
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email wajib diisi"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Format email tidak valid"
                false
            }
            phone.isEmpty() -> {
                binding.etPhone.error = "Nomor HP wajib diisi"
                false
            }
            address.isEmpty() -> {
                binding.etAddress.error = "Alamat wajib diisi"
                false
            }
            city.isEmpty() -> {
                binding.etCity.error = "Kota wajib diisi"
                false
            }
            province.isEmpty() -> {
                binding.etProvince.error = "Provinsi wajib diisi"
                false
            }
            postalCode.isEmpty() -> {
                binding.etPostalCode.error = "Kode pos wajib diisi"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password wajib diisi"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Password minimal 6 karakter"
                false
            }
            confirmPassword != password -> {
                binding.etConfirmPassword.error = "Password tidak sama"
                false
            }
            else -> true
        }
    }

    private fun performRegister(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        address: String,
        city: String,
        province: String,
        postalCode: String
    ) {
        // body JSON-nya sekarang udah lengkap sama address
        val request = RegisterRequest(
            username = username,
            fullName = fullName,
            email = email,
            phone = phone,
            password = password,
            address = address,
            city = city,
            province = province,
            postalCode = postalCode
        )

        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.register(request)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.success == true) {
                        Toast.makeText(
                            requireContext(),
                            body.message.ifEmpty { "Registrasi berhasil, silakan login." },
                            Toast.LENGTH_LONG
                        ).show()

                        // langsung lempar ke login
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, LoginFragment())
                            .commit()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Registrasi gagal.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val errorText = response.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}: ${errorText ?: "Gagal registrasi"}",
                        Toast.LENGTH_LONG
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
        binding.progressBarRegister.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
