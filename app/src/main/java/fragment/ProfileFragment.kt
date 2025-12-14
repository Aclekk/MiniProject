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
    private var userRole: String = "buyer"

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

    private fun checkUserRole() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = prefs.getString("role", "buyer") ?: "buyer"
        setupUIForRole()
    }

    private fun setupUIForRole() {
        if (userRole == "seller" || userRole == "admin") {

            binding.tilStoreAddress.visibility = View.VISIBLE
            binding.btnViewReviews.visibility = View.VISIBLE
            binding.btnViewSalesReport.visibility = View.VISIBLE

        } else {

            binding.tilStoreAddress.visibility = View.GONE
            binding.btnViewReviews.visibility = View.GONE
            binding.btnViewSalesReport.visibility = View.GONE
        }
    }

    private fun setupClicks() {

        binding.btnViewReviews.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReviewListFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnViewSalesReport.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SalesReportFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun loadProfile() {
        if (userRole == "seller") loadStoreSettings() else loadUserProfile()
    }

    private fun loadUserProfile() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        lifecycleScope.launch {
            val response = ApiClient.apiService.getProfile("Bearer $token")
            if (response.isSuccessful && response.body()?.data != null) {
                bindProfileToUi(response.body()!!.data!!)
            }
        }
    }

    private fun bindProfileToUi(data: ProfileData) {
        binding.etUsername.setText(data.user.username)
        binding.etFullName.setText(data.user.fullName)
        binding.etEmail.setText(data.user.email)
        binding.etPhone.setText(data.user.phone)

        Glide.with(this)
            .load(ApiClient.getImageUrl(data.user.profileImage))
            .placeholder(R.drawable.ic_person)
            .into(binding.imgProfile)
    }

    private fun loadStoreSettings() {}

    private fun logout() {
        requireActivity()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
            .edit().clear().apply()

        startActivity(Intent(requireContext(), WelcomeActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
