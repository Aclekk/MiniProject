package com.example.miniproject.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.MainActivity
import com.example.miniproject.R
import com.example.miniproject.databinding.FragmentStoreProfileBinding
import java.io.InputStream

class StoreProfileFragment : Fragment() {

    private var _binding: FragmentStoreProfileBinding? = null
    private val binding get() = _binding!!
    private val PICK_IMAGE = 100
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfileData()

        // 📸 Ganti foto toko
        binding.btnChangePhoto.setOnClickListener {
            openGallery()
        }

        // 💾 Simpan profil toko
        binding.btnSaveProfile.setOnClickListener {
            saveProfileData()
        }

        // 🚪 Tombol Logout (kalau kamu tambahkan di layout)
        binding.btnLogout?.setOnClickListener {
            hideKeyboard()
            (activity as? MainActivity)?.onLogout()
            Toast.makeText(requireContext(), "Logout berhasil ✅", Toast.LENGTH_SHORT).show()
        }

        // ✋ Klik area kosong = tutup keyboard
        binding.root.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                v.clearFocus()
            }
            false
        }
    }

    // ==========================
    // 📷 FOTO TOKO
    // ==========================
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            val inputStream: InputStream? = imageUri?.let {
                requireContext().contentResolver.openInputStream(it)
            }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.ivStorePhoto.setImageBitmap(bitmap)
            saveImageUri(imageUri.toString())
        }
    }

    private fun saveImageUri(uri: String) {
        val prefs = requireContext().getSharedPreferences("store_pref", Context.MODE_PRIVATE)
        prefs.edit().putString("photo_uri", uri).apply()
    }

    // ==========================
    // 💾 SIMPAN & LOAD DATA
    // ==========================
    private fun saveProfileData() {
        val prefs = requireContext().getSharedPreferences("store_pref", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("store_name", binding.etStoreName.text.toString())
            putString("store_address", binding.etStoreAddress.text.toString())
            putString("store_contact", binding.etStoreContact.text.toString())
            apply()
        }
        hideKeyboard()
        Toast.makeText(requireContext(), "Profil toko disimpan ✅", Toast.LENGTH_SHORT).show()
    }

    private fun loadProfileData() {
        val prefs = requireContext().getSharedPreferences("store_pref", Context.MODE_PRIVATE)

        binding.etStoreName.setText(prefs.getString("store_name", "Toko Niaga Tani"))
        binding.etStoreAddress.setText(prefs.getString("store_address", "Jl. Pertanian No. 1"))
        binding.etStoreContact.setText(prefs.getString("store_contact", "08123456789"))

        val photoUri = prefs.getString("photo_uri", null)
        if (photoUri != null) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(Uri.parse(photoUri))
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.ivStorePhoto.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================
    // 🧠 UTIL: Tutup Keyboard
    // ==========================
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
