package com.example.miniproject.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.miniproject.MainActivity
import com.example.miniproject.R
import com.example.miniproject.databinding.FragmentProfileBinding
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val CAMERA_REQUEST = 101
    private val GALLERY_REQUEST = 102
    private lateinit var photoFile: File
    private var currentPhotoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 Cek role user (admin → store profile)
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val role = sharedPref.getString("role", "user")
        if (role == "admin") {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StoreProfileFragment())
                .commit()
            return
        }

        // 🔹 Load profil user sebelumnya
        loadUserProfile()

        // 🔹 Ganti foto (kamera / galeri)
        binding.btnTakePhoto.setOnClickListener { showImagePickerOptions() }

        // 🔹 Simpan profil
        binding.btnSaveProfile.setOnClickListener { saveUserProfile() }

        // 🔹 Tombol Store Profile (khusus admin)
        binding.btnStoreProfile.visibility = View.GONE

        // 🔹 Logout
        binding.btnLogout.setOnClickListener {
            (activity as? MainActivity)?.onLogout()
            Toast.makeText(requireContext(), "Logout berhasil!", Toast.LENGTH_SHORT).show()
        }
    }

    // ======================================================
    // 📸 PILIH OPSI FOTO: Kamera atau Galeri
    // ======================================================
    private fun showImagePickerOptions() {
        val options = arrayOf("Ambil foto dari kamera", "Pilih dari galeri")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih foto profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }

    // ======================================================
    // 📸 Kamera
    // ======================================================
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            photoFile = File(requireContext().getExternalFilesDir(null), "profile_photo.jpg")
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            currentPhotoUri = uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, CAMERA_REQUEST)
        } else {
            Toast.makeText(requireContext(), "Kamera tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    // ======================================================
    // 🖼️ Galeri
    // ======================================================
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    // ======================================================
    // 🔄 Hasil ambil foto / galeri
    // ======================================================
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    currentPhotoUri?.let {
                        binding.imgProfile.setImageURI(it)
                        savePhotoUri(it.toString())
                        Toast.makeText(requireContext(), "Foto profil diperbarui ✅", Toast.LENGTH_SHORT).show()
                    }
                }
                GALLERY_REQUEST -> {
                    val uri = data?.data
                    if (uri != null) {
                        binding.imgProfile.setImageURI(uri)
                        savePhotoUri(uri.toString())
                        Toast.makeText(requireContext(), "Foto profil diperbarui ✅", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // ======================================================
    // 💾 Simpan ke SharedPreferences
    // ======================================================
    private fun savePhotoUri(uri: String) {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        prefs.edit().putString("profile_photo_uri", uri).apply()
    }

    private fun saveUserProfile() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val address = binding.etAddress.text.toString()

        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("name", name)
            putString("email", email)
            putString("address", address)
            apply()
        }

        Toast.makeText(requireContext(), "Profil disimpan ✅", Toast.LENGTH_SHORT).show()
    }

    // ======================================================
    // 🔄 Load data saat buka fragment
    // ======================================================
    private fun loadUserProfile() {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        binding.etName.setText(prefs.getString("name", "Rachen 🌾"))
        binding.etEmail.setText(prefs.getString("email", "rachen@example.com"))
        binding.etAddress.setText(prefs.getString("address", "Jl. Sawah Indah No. 7, Tangerang"))

        val photoUri = prefs.getString("profile_photo_uri", null)
        if (photoUri != null) {
            try {
                binding.imgProfile.setImageURI(Uri.parse(photoUri))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ======================================================
    // 📱 Permission result
    // ======================================================
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
