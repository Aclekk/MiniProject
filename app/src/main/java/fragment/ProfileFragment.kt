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
    private var userRole: String = "buyer" // Default buyer

    companion object {
        private const val REQ_PICK_IMAGE = 101
        private const val REQ_PICK_LOGO = 102
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

        checkUserRole()
        setupClicks()
        loadProfile()
    }

    // ================== CEK ROLE USER ==================

    private fun checkUserRole() {
        val prefs = requireActivity()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        userRole = prefs.getString("role", "buyer") ?: "buyer"

        // Setup UI berdasarkan role
        setupUIForRole()
    }

    private fun setupUIForRole() {
        if (userRole == "seller") {
            // Mode Seller - Ubah label dan enable edit
            binding.tvSectionTitle.text = "🏪 Informasi Toko"
            binding.btnSaveProfile.text = "💾 Simpan Profil Toko"
            binding.tvPhotoLabel.text = "📷 Ketuk untuk mengganti logo toko"

            // Tampilkan field alamat toko
            binding.tilStoreAddress.visibility = View.VISIBLE

            // Enable edit untuk seller
            binding.etUsername.isEnabled = true
            binding.etFullName.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.etPhone.isEnabled = true
            binding.etAddress.isEnabled = true
            binding.etStoreAddress.isEnabled = true

            // Ubah hint sesuai data toko
            binding.tilUsername.hint = "No WhatsApp"
            binding.tilFullName.hint = "Nama Toko"
            binding.tilEmail.hint = "Email Toko"
            binding.tilPhone.hint = "No Telepon Toko"
            binding.tilAddress.hint = "Tagline/Deskripsi Toko"
            binding.tilStoreAddress.hint = "Alamat Toko"

        } else {
            // Mode Buyer - Semua read-only kecuali foto
            binding.tvSectionTitle.text = "📋 Informasi Profil"
            binding.btnSaveProfile.text = "💾 Simpan Foto Profil"
            binding.tvPhotoLabel.text = "📷 Ketuk untuk mengganti foto"

            // SEMBUNYIKAN field alamat toko untuk buyer
            binding.tilStoreAddress.visibility = View.GONE

            binding.etUsername.isEnabled = false
            binding.etFullName.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etPhone.isEnabled = false
            binding.etAddress.isEnabled = false
            binding.etStoreAddress.isEnabled = false

            // Hint default
            binding.tilUsername.hint = "Username"
            binding.tilFullName.hint = "Nama Lengkap"
            binding.tilEmail.hint = "Email"
            binding.tilPhone.hint = "No Telepon"
            binding.tilAddress.hint = "Alamat"
            // tilStoreAddress nggak kelihatan, jadi hint-nya nggak penting
        }
    }


    // ================== CLICK HANDLERS ==================

    private fun setupClicks() {
        // Klik foto profil / logo
        binding.cardProfile.setOnClickListener {
            if (userRole == "seller") {
                openGallery(REQ_PICK_LOGO) // Upload logo toko
            } else {
                openGallery(REQ_PICK_IMAGE) // Upload foto profil user
            }
        }
        binding.btnTakePhoto.setOnClickListener {
            if (userRole == "seller") {
                openGallery(REQ_PICK_LOGO)
            } else {
                openGallery(REQ_PICK_IMAGE)
            }
        }

        // Tombol simpan
        binding.btnSaveProfile.setOnClickListener {
            if (userRole == "seller") {
                updateStoreSettings()
            } else {
                updateProfilePhotoOnly()
            }
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    // ================== LOAD PROFILE ==================

    private fun loadProfile() {
        if (userRole == "seller") {
            // Seller: Load settings toko dari database
            loadStoreSettings()
        } else {
            // Buyer: Load profile user dari API
            loadUserProfile()
        }
    }

    private fun loadUserProfile() {
        val prefs = requireActivity()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)

        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Belum login", Toast.LENGTH_SHORT).show()
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
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun bindProfileToUi(data: ProfileData) {
        val user = data.user
        val address = data.addresses?.firstOrNull()

        binding.etUsername.setText(user.username)
        binding.etFullName.setText(user.fullName)
        binding.etEmail.setText(user.email)
        binding.etPhone.setText(user.phone)

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

        val imageUrl = ApiClient.getImageUrl(user.profileImage)
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(binding.imgProfile)
    }

    // ================== LOAD SETTINGS TOKO (SELLER) ==================

    private fun loadStoreSettings() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getSettings()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        @Suppress("UNCHECKED_CAST")
                        bindStoreSettingsToUi(body.data as Map<String, Any>)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal memuat settings toko",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun bindStoreSettingsToUi(settings: Map<String, Any>) {
        // Isi field dengan data toko dari database settings
        binding.etFullName.setText(settings["app_name"]?.toString() ?: "")
        binding.etEmail.setText(settings["contact_email"]?.toString() ?: "")
        binding.etPhone.setText(settings["contact_phone"]?.toString() ?: "")
        binding.etUsername.setText(settings["contact_whatsapp"]?.toString() ?: "")
        binding.etAddress.setText(settings["app_tagline"]?.toString() ?: "")
        binding.etStoreAddress.setText(settings["app_address"]?.toString() ?: "")

        // Load logo toko
        val logoPath = settings["app_logo"]?.toString()
        if (!logoPath.isNullOrEmpty()) {
            val logoUrl = ApiClient.getImageUrl(logoPath)
            Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imgProfile)
        }
    }

    // ================== UPDATE SETTINGS TOKO (SELLER) ==================

    private fun updateStoreSettings() {
        val prefs = requireActivity()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token hilang, login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil data dari form
        val appName = binding.etFullName.text.toString().trim()
        val contactEmail = binding.etEmail.text.toString().trim()
        val contactPhone = binding.etPhone.text.toString().trim()
        val appTagline = binding.etAddress.text.toString().trim()
        val contactWhatsapp = binding.etUsername.text.toString().trim()
        val appAddress = binding.etStoreAddress.text.toString().trim()

        if (appName.isEmpty() || contactEmail.isEmpty() || contactPhone.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Nama toko, email, dan telepon wajib diisi",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        showLoading(true)

        // Jika ada logo yang dipilih, upload dulu
        if (selectedImagePart != null) {
            uploadLogoFirst(token) { logoUploaded ->
                if (logoUploaded) {
                    updateStoreSettingsText(
                        token,
                        appName,
                        contactEmail,
                        contactPhone,
                        appTagline,
                        contactWhatsapp,
                        appAddress
                    )
                } else {
                    showLoading(false)
                }
            }
        } else {
            // Langsung update settings text
            updateStoreSettingsText(
                token,
                appName,
                contactEmail,
                contactPhone,
                appTagline,
                contactWhatsapp,
                appAddress
            )
        }
    }

    private fun uploadLogoFirst(token: String, callback: (Boolean) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.uploadStoreLogo(
                    token = "Bearer $token",
                    logo = selectedImagePart!!
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Logo berhasil diupload", Toast.LENGTH_SHORT).show()
                    selectedImagePart = null
                    callback(true)
                } else {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("UploadLogo", "code=$code body=$errorBody")
                    Toast.makeText(requireContext(), "Gagal upload logo ($code)", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            } catch (e: Exception) {
                android.util.Log.e("UploadLogo", "Exception: ${e.localizedMessage}", e)
                Toast.makeText(requireContext(), "Error upload: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        }
    }


    private fun updateStoreSettingsText(
        token: String,
        appName: String,
        contactEmail: String,
        contactPhone: String,
        appTagline: String,
        contactWhatsapp: String,
        appAddress: String
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val requestBody = mapOf(
                    "app_name" to appName,
                    "contact_email" to contactEmail,
                    "contact_phone" to contactPhone,
                    "app_tagline" to appTagline,
                    "contact_whatsapp" to contactWhatsapp,
                    "app_address" to appAddress
                )

                val response = ApiClient.apiService.updateSettings(
                    token = "Bearer $token",
                    request = requestBody
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(
                            requireContext(),
                            "✅ Profil toko berhasil diperbarui!",
                            Toast.LENGTH_LONG
                        ).show()

                        // Reload settings
                        loadStoreSettings()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal update settings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    // ================== UPDATE FOTO PROFIL (BUYER) ==================

    private fun updateProfilePhotoOnly() {
        val prefs = requireActivity()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token hilang", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImagePart == null) {
            Toast.makeText(requireContext(), "Pilih foto dulu", Toast.LENGTH_SHORT).show()
            return
        }

        fun emptyPart(): RequestBody? = null

        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.updateProfile(
                    token = "Bearer $token",
                    fullName = emptyPart(),
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
                            "✅ Foto profil berhasil diperbarui!",
                            Toast.LENGTH_SHORT
                        ).show()
                        bindProfileToUi(body.data)
                        selectedImagePart = null
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal update",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    // ================== PICK IMAGE ==================

    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri = data.data ?: return

            when (requestCode) {
                REQ_PICK_IMAGE -> {
                    // Buyer: upload foto profil user
                    prepareImagePart(uri, "profile_image")
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.imgProfile)
                }

                REQ_PICK_LOGO -> {
                    // Seller: upload logo toko
                    prepareImagePart(uri, "logo") // field name "logo" untuk API upload_logo.php
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.imgProfile)

                    Toast.makeText(
                        requireContext(),
                        "Logo siap diupload. Klik Simpan Profil Toko",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun prepareImagePart(uri: Uri, fieldName: String) {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()
        inputStream.close()

        val mediaType = "image/*".toMediaTypeOrNull()
        val requestBody = bytes.toRequestBody(mediaType, 0, bytes.size)
        selectedImagePart = MultipartBody.Part.createFormData(
            fieldName,
            "${fieldName}_${System.currentTimeMillis()}.jpg",
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
