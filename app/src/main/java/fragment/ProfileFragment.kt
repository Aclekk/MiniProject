package com.example.miniproject.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.miniproject.MainActivity
import com.example.miniproject.R
import com.example.miniproject.databinding.FragmentProfileBinding
import com.example.miniproject.model.StoreProfile
import com.example.miniproject.model.UserProfile // ✅ TAMBAHAN

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_GALLERY = 100
    private val REQUEST_CAMERA = 101
    private val REQ_PERM_CAMERA = 201
    private val REQ_PERM_READ = 202

    private var selectedPhotoUri: Uri? = null
    private var isAdminMode = false // ✅ TAMBAHAN: flag untuk cek mode

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isAdmin = (activity as? MainActivity)?.isAdmin() ?: false
        isAdminMode = isAdmin // ✅ Simpan status admin

        if (isAdmin) setupAdminMode() else setupUserMode()

        binding.btnLogout.setOnClickListener {
            (activity as? MainActivity)?.onLogout()
            Toast.makeText(requireContext(), "Logout berhasil!", Toast.LENGTH_SHORT).show()
        }
    }

    /** 🔑 ADMIN MODE **/
    private fun setupAdminMode() {
        binding.tvSectionTitle.text = "🏪 Kelola Profil Toko"

        // ✅ Load data dari StoreProfile (khusus admin)
        binding.etName.setText(StoreProfile.storeName)
        binding.etEmail.setText(StoreProfile.storeContact)
        binding.etAddress.setText(StoreProfile.storeAddress)
        binding.etAbout.setText(StoreProfile.storeAbout)

        binding.etAbout.visibility = View.GONE

        // ✅ Load foto toko dari StoreProfile
        StoreProfile.storePhotoUri?.let { binding.imgProfile.setImageURI(it) }


        binding.btnTakePhoto.setOnClickListener { showPhotoPickerDialog() }

        binding.btnSaveProfile.visibility = View.VISIBLE
        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val contact = binding.etEmail.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val about = binding.etAbout.text.toString().trim()

            if (name.isNotEmpty() && contact.isNotEmpty() && address.isNotEmpty()) {
                // ✅ Update STORE PROFILE (khusus admin)
                StoreProfile.updateProfile(name, address, contact, about, selectedPhotoUri)
                Toast.makeText(requireContext(), "✅ Profil toko berhasil diperbarui!", Toast.LENGTH_SHORT).show()

                // ✅ Update langsung ProductsFragment yang aktif
                refreshProductsFragment()
            } else {
                Toast.makeText(requireContext(), "⚠️ Lengkapi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        // 🆕 Tombol laporan penjualan
        binding.btnSalesReport.visibility = View.VISIBLE
        binding.btnSalesReport.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SalesReportFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🆕 Tombol lihat semua ulasan
        binding.btnViewReviews.visibility = View.VISIBLE
        binding.btnViewReviews.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReviewListFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🆕 Tombol kirim notifikasi
        binding.btnSendNotification.visibility = View.VISIBLE
        binding.btnSendNotification.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SendNotificationFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /** 👤 USER MODE **/
    private fun setupUserMode() {
        val username = (activity as? MainActivity)?.getCurrentUsername() ?: "User"
        binding.tvSectionTitle.text = "👤 Profil Saya"

        // ✅ Load data dari UserProfile (khusus user)
        binding.etName.setText(UserProfile.userName)
        binding.etEmail.setText(UserProfile.userEmail)
        binding.etAddress.setText(UserProfile.userAddress)
        binding.etAbout.visibility = View.GONE


        // ✅ Load foto profil user
        UserProfile.userPhotoUri?.let { binding.imgProfile.setImageURI(it) }


        binding.btnTakePhoto.setOnClickListener { showPhotoPickerDialog() }

        binding.etName.isEnabled = true
        binding.etEmail.isEnabled = true
        binding.etAddress.isEnabled = true

        binding.btnSaveProfile.visibility = View.VISIBLE
        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && address.isNotEmpty()) {
                // ✅ Update USER PROFILE (khusus user)
                UserProfile.updateProfile(name, email, address, selectedPhotoUri)
                Toast.makeText(requireContext(), "✅ Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "⚠️ Lengkapi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

        // 🆕 Tombol lihat notifikasi untuk USER
        binding.btnViewNotifications.visibility = View.VISIBLE
        binding.btnViewNotifications.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationListFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /** 📸 PILIH FOTO **/
    private fun showPhotoPickerDialog() {
        val options = arrayOf("Pilih dari Galeri", "Ambil dari Kamera")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Sumber Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkReadPermission()
                    1 -> checkCameraPermission()
                }
            }.show()
    }

    private fun checkReadPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission),
                REQ_PERM_READ
            )
        } else {
            openGallery()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQ_PERM_CAMERA
            )
        } else {
            openCamera()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }

        if (intent.resolveActivity(requireContext().packageManager) == null) {
            intent.action = Intent.ACTION_GET_CONTENT
        }

        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivityForResult(intent, REQUEST_CAMERA)
        } else {
            Toast.makeText(requireContext(), "📷 Kamera tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            Log.w("ProfileFragment", "Activity result not OK: $resultCode")
            return
        }

        when (requestCode) {
            REQUEST_GALLERY -> {
                val uri = data?.data
                if (uri != null) {
                    try {
                        requireContext().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        Log.w("ProfileFragment", "Persistent permission denied, continue anyway")
                    }

                    selectedPhotoUri = uri
                    binding.imgProfile.setImageURI(uri)

                    // ✅ PENTING: Cek mode admin atau user!
                    if (isAdminMode) {
                        // ADMIN → Update logo toko
                        StoreProfile.storePhotoUri = uri
                        Toast.makeText(requireContext(), "✅ Foto toko dari galeri dipilih!", Toast.LENGTH_SHORT).show()
                        refreshProductsFragment() // Update logo di ProductsFragment
                    } else {
                        // USER → Update foto profil user sendiri
                        UserProfile.userPhotoUri = uri
                        Toast.makeText(requireContext(), "✅ Foto profil dari galeri dipilih!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "⚠️ Gagal memilih foto", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_CAMERA -> {
                val bitmap = data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    binding.imgProfile.setImageBitmap(bitmap)

                    // ✅ Untuk camera, kita hanya set ke ImageView
                    // Kalau mau persisten, harus save bitmap ke file dulu
                    if (isAdminMode) {
                        Toast.makeText(requireContext(), "✅ Foto toko dari kamera berhasil diambil!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "✅ Foto profil dari kamera berhasil diambil!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "⚠️ Gagal mengambil foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshProductsFragment() {
        parentFragmentManager.fragments.forEach { fragment ->
            if (fragment is ProductsFragment && fragment.isAdded) {
                fragment.updateStoreInfo()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "⚠️ Izin ditolak", Toast.LENGTH_SHORT).show()
            return
        }

        when (requestCode) {
            REQ_PERM_READ -> openGallery()
            REQ_PERM_CAMERA -> openCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}