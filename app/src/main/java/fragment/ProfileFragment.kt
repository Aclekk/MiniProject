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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.WelcomeActivity
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.ProfileData
import com.example.miniproject.databinding.FragmentProfileBinding
import com.example.miniproject.viewmodel.ProfileViewModel
import com.example.miniproject.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedImagePart: MultipartBody.Part? = null
    private var userRole: String = "buyer"

    // ✅ MVVM: ViewModel
    private lateinit var profileViewModel: ProfileViewModel

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

        // ✅ MVVM: Initialize ViewModel
        setupViewModel()

        checkUserRole()
        setupClicks()
        loadProfile()
        setupSellerButtons()
    }

    // ================== MVVM SETUP ==================

    private fun setupViewModel() {
        val factory = ProfileViewModelFactory(ApiClient.apiService)
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
        setupObservers()
    }

    private fun setupObservers() {
        // Observe profile data (BUYER)
        profileViewModel.profileData.observe(viewLifecycleOwner) { data ->
            bindProfileToUi(data)
        }

        // Observe store settings (SELLER)
        profileViewModel.storeSettings.observe(viewLifecycleOwner) { settings ->
            bindStoreSettingsToUi(settings)
        }

        // Observe loading state
        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        // Observe error messages
        profileViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearError()
            }
        }

        // Observe success messages
        profileViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearSuccess()
            }
        }
    }

    // ================== USER ROLE & UI SETUP ==================

    private fun checkUserRole() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = prefs.getString("role", "buyer") ?: "buyer"
        setupUIForRole()
    }

    private fun setupUIForRole() {
        if (userRole == "seller") {
            // ✅ SELLER MODE
            binding.tvSectionTitle.text = "🏪 Informasi Toko"
            binding.btnSaveProfile.text = "💾 Simpan Profil Toko"

            binding.tilStoreAddress.visibility = View.VISIBLE

            // ✅ Show seller-specific sections
            binding.root.findViewById<View>(R.id.dividerSeller)?.visibility = View.VISIBLE
            binding.root.findViewById<View>(R.id.sectionStoreInfo)?.visibility = View.VISIBLE

            // ✅ Show seller buttons
            binding.root.findViewById<View>(R.id.cardViewReviews)?.visibility = View.VISIBLE
            binding.root.findViewById<View>(R.id.cardSalesReport)?.visibility = View.VISIBLE

            // Enable all seller fields
            binding.etUsername.isEnabled = true
            binding.etFullName.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.etPhone.isEnabled = true
            binding.etAddress.isEnabled = true
            binding.etStoreAddress.isEnabled = true

            // Seller-specific hints
            binding.tilUsername.hint = "📱 No WhatsApp"
            binding.tilFullName.hint = "🏪 Nama Toko"
            binding.tilEmail.hint = "📧 Email Toko"
            binding.tilPhone.hint = "📱 No Telepon Toko"
            binding.tilAddress.hint = "✨ Tagline/Deskripsi Toko"
            binding.tilStoreAddress.hint = "🏪 Alamat Toko"

        } else {
            // ✅ BUYER MODE
            binding.tvSectionTitle.text = "👤 Informasi Akun"
            binding.btnSaveProfile.text = "💾 Simpan Perubahan"

            binding.tilStoreAddress.visibility = View.GONE

            // ✅ Hide seller-specific sections
            binding.root.findViewById<View>(R.id.dividerSeller)?.visibility = View.GONE
            binding.root.findViewById<View>(R.id.sectionStoreInfo)?.visibility = View.GONE

            // ✅ Hide seller buttons
            binding.root.findViewById<View>(R.id.cardViewReviews)?.visibility = View.GONE
            binding.root.findViewById<View>(R.id.cardSalesReport)?.visibility = View.GONE

            // ✅ Buyer can edit all fields except username
            binding.etUsername.isEnabled = false  // Username tidak bisa diganti
            binding.etFullName.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.etPhone.isEnabled = true
            binding.etAddress.isEnabled = true
            binding.etStoreAddress.isEnabled = false

            // Buyer-specific hints
            binding.tilUsername.hint = "📱 Username"
            binding.tilFullName.hint = "👨 Nama Lengkap"
            binding.tilEmail.hint = "📧 Email"
            binding.tilPhone.hint = "📞 No Telepon"
            binding.tilAddress.hint = "🏠 Alamat"
        }
    }

    private fun setupSellerButtons() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val role = prefs.getString("role", "buyer") ?: "buyer"

        val isSeller = role == "seller" || role == "admin"

        if (isSeller) {
            // ✅ Setup click listeners untuk seller buttons
            binding.root.findViewById<View>(R.id.btnViewReviews)?.setOnClickListener {
                val frag = ReviewListFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .addToBackStack(null)
                    .commit()
            }

            binding.root.findViewById<View>(R.id.btnViewSalesReport)?.setOnClickListener {
                val frag = SalesReportFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    // ================== CLICK LISTENERS ==================

    private fun setupClicks() {
        binding.cardProfile.setOnClickListener {
            if (userRole == "seller") openGallery(REQ_PICK_LOGO)
            else openGallery(REQ_PICK_IMAGE)
        }

        binding.btnTakePhoto.setOnClickListener {
            if (userRole == "seller") openGallery(REQ_PICK_LOGO)
            else openGallery(REQ_PICK_IMAGE)
        }

        binding.btnSaveProfile.setOnClickListener {
            if (userRole == "seller") updateStoreSettings()
            else updateBuyerProfile()
        }

        binding.btnLogout.setOnClickListener { logout() }
    }

    // ================== LOAD PROFILE (MVVM) ==================

    private fun loadProfile() {
        if (userRole == "seller") {
            // ✅ MVVM: Load via ViewModel
            profileViewModel.loadStoreSettings()
        } else {
            // ✅ MVVM: Load via ViewModel
            val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            val token = prefs.getString("token", null)

            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Belum login", Toast.LENGTH_SHORT).show()
                return
            }

            profileViewModel.loadUserProfile(token)
        }
    }

    // ================== BIND DATA TO UI ==================

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
        } else ""

        binding.etAddress.setText(addressText)

        Glide.with(this)
            .load(ApiClient.getImageUrl(user.profileImage))
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(binding.imgProfile)
    }

    private fun bindStoreSettingsToUi(settings: Map<String, Any>) {
        binding.etFullName.setText(settings["app_name"]?.toString() ?: "")
        binding.etEmail.setText(settings["contact_email"]?.toString() ?: "")
        binding.etPhone.setText(settings["contact_phone"]?.toString() ?: "")
        binding.etUsername.setText(settings["contact_whatsapp"]?.toString() ?: "")
        binding.etAddress.setText(settings["app_tagline"]?.toString() ?: "")
        binding.etStoreAddress.setText(settings["app_address"]?.toString() ?: "")

        val logoPath = settings["app_logo"]?.toString()
        if (!logoPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(ApiClient.getImageUrl(logoPath))
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imgProfile)
        }
    }

    // ================== UPDATE PROFILE (MVVM) ==================

    private fun updateBuyerProfile() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token hilang", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        if (fullName.isEmpty()) {
            Toast.makeText(requireContext(), "Nama lengkap tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        fun createPart(value: String): RequestBody {
            return value.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        // ✅ MVVM: Update via ViewModel
        profileViewModel.updateBuyerProfile(
            token = token,
            fullName = createPart(fullName),
            email = createPart(email),
            phone = if (phone.isNotEmpty()) createPart(phone) else null,
            address = if (address.isNotEmpty()) createPart(address) else null,
            profileImage = selectedImagePart
        )

        // Clear selected image after update
        selectedImagePart = null
    }

    private fun updateStoreSettings() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token hilang, login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        val appName = binding.etFullName.text.toString().trim()
        val contactEmail = binding.etEmail.text.toString().trim()
        val contactPhone = binding.etPhone.text.toString().trim()
        val appTagline = binding.etAddress.text.toString().trim()
        val contactWhatsapp = binding.etUsername.text.toString().trim()
        val appAddress = binding.etStoreAddress.text.toString().trim()

        if (appName.isEmpty() || contactEmail.isEmpty() || contactPhone.isEmpty()) {
            Toast.makeText(requireContext(), "Nama toko, email, dan telepon wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ MVVM: Upload logo first if exists
        if (selectedImagePart != null) {
            profileViewModel.uploadStoreLogo(token, selectedImagePart!!) {
                // After logo uploaded, update settings
                profileViewModel.updateStoreSettings(
                    token, appName, contactEmail, contactPhone, appTagline, contactWhatsapp, appAddress
                )
                selectedImagePart = null
            }
        } else {
            // ✅ MVVM: Update settings directly
            profileViewModel.updateStoreSettings(
                token, appName, contactEmail, contactPhone, appTagline, contactWhatsapp, appAddress
            )
        }
    }

    // ================== IMAGE PICKER ==================

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
                    prepareImagePart(uri, "profile_image")
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.imgProfile)
                }
                REQ_PICK_LOGO -> {
                    prepareImagePart(uri, "logo")
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_person)
                        .into(binding.imgProfile)
                    Toast.makeText(requireContext(), "Logo siap diupload. Klik Simpan Profil Toko", Toast.LENGTH_SHORT).show()
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
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // ================== LOADING STATE ==================

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !show
        binding.btnLogout.isEnabled = !show

        binding.root.findViewById<View>(R.id.btnViewReviews)?.isEnabled = !show
        binding.root.findViewById<View>(R.id.btnViewSalesReport)?.isEnabled = !show
    }

    // ================== CLEANUP ==================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}