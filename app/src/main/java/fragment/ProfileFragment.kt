package com.example.miniproject.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.WelcomeActivity
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.ProfileData
import com.example.miniproject.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedImagePart: MultipartBody.Part? = null

    companion object {
        private const val REQ_PICK_IMAGE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        loadProfile()
    }

    // ================== CLICK HANDLERS ==================

    private fun setupClicks() {
        // klik kartu atau overlay buat ganti foto
        binding.cardProfile.setOnClickListener { openGallery() }
        binding.btnTakePhoto.setOnClickListener { openGallery() }

        // cuma simpan FOTO, data lain read-only
        binding.btnSaveProfile.setOnClickListener {
            updateProfilePhotoOnly()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    // ================== LOAD PROFILE DARI API ==================

    private fun loadProfile() {
        val prefs = requireActivity()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Belum login, silakan login dulu", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getProfile("Bearer $token")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        bindProfileToUi(body.data)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal memuat profil",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()} saat getProfile",
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

    private fun bindProfileToUi(data: ProfileData) {
        val user = data.user
        val address = data.addresses?.firstOrNull()

        // isi field sesuai layout BARU
        binding.etUsername.setText(user.username)
        binding.etFullName.setText(user.fullName)
        binding.etEmail.setText(user.email)
        binding.etPhone.setText(user.phone)

        // gabung alamat: "Jl X, Kota Y, Prov Z 12345"
        val addressText = if (address != null) {
            buildString {
                append(address.address ?: "")
                if (!address.city.isNullOrEmpty()) {
                    if (isNotEmpty()) append(", ")
                    append(address.city)
                }
                if (!address.province.isNullOrEmpty()) {
                    if (isNotEmpty()) append(", ")
                    append(address.province)
                }
                if (!address.postalCode.isNullOrEmpty()) {
                    append(" ")
                    append(address.postalCode)
                }
            }
        } else {
            ""
        }
        binding.etAddress.setText(addressText)

        // foto profil
        val imageUrl = ApiClient.getImageUrl(user.profileImage)
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(binding.imgProfile)
    }

    // ================== UPDATE: FOTO DOANG ==================

    private fun updateProfilePhotoOnly() {
        val prefs = requireActivity()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token hilang, login ulang dulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImagePart == null) {
            Toast.makeText(requireContext(), "Pilih foto dulu ngab", Toast.LENGTH_SHORT).show()
            return
        }

        // semua field teks dikosongin biar backend cuma update foto
        fun emptyPart(): RequestBody? = null

        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.updateProfile(
                    token = "Bearer $token",
                    fullName = emptyPart(),       // ga diupdate
                    email = emptyPart(),
                    phone = emptyPart(),
                    address = emptyPart(),
                    city = emptyPart(),
                    province = emptyPart(),
                    postalCode = emptyPart(),
                    recipientName = emptyPart(),
                    recipientPhone = emptyPart(),
                    profileImage = selectedImagePart
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Toast.makeText(
                            requireContext(),
                            body.message.ifEmpty { "Foto profil berhasil diperbarui" },
                            Toast.LENGTH_LONG
                        ).show()
                        // refresh tampilan (kalau backend balikin URL baru)
                        bindProfileToUi(body.data)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal update foto profil",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val err = response.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}: ${err ?: "Gagal update"}",
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

    // ================== PICK IMAGE ==================

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQ_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri = data.data ?: return
            prepareImagePart(uri)

            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .into(binding.imgProfile)
        }
    }

    private fun prepareImagePart(uri: Uri) {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()

        val mediaType = "image/*".toMediaTypeOrNull()
        val requestBody = bytes.toRequestBody(mediaType, 0, bytes.size)
        selectedImagePart = MultipartBody.Part.createFormData(
            "profile_image",
            "profile_${System.currentTimeMillis()}.jpg",
            requestBody
        )
    }

    // ================== LOGOUT ==================

    private fun logout() {
        val prefs = requireActivity()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        prefs.edit()
            .clear()
            .apply()

        Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // ================== UTIL ==================

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
