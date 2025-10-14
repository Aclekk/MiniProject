package com.example.miniproject.fragment

import android.Manifest
import android.app.Activity
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

    private lateinit var photoFile: File
    private val CAMERA_REQUEST = 101
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

        // ✅ Isi data dummy
        binding.etName.setText("Rachen 🌾")
        binding.etEmail.setText("rachen@example.com")
        binding.etAddress.setText("Jl. Sawah Indah No. 7, Tangerang")

        // 📸 Ambil / ubah foto
        binding.btnTakePhoto.setOnClickListener {
            checkCameraPermission()
        }

        // 💾 Tombol Simpan profil
        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val address = binding.etAddress.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && address.isNotEmpty()) {
                Toast.makeText(requireContext(), "Profil disimpan ✅", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Lengkapi semua data dulu!", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔴 Logout
        binding.btnLogout.setOnClickListener {
            (activity as? MainActivity)?.onLogout()
            Toast.makeText(requireContext(), "Logout berhasil!", Toast.LENGTH_SHORT).show()
        }
    }

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
            Toast.makeText(requireContext(), "Kamera tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            currentPhotoUri?.let {
                binding.imgProfile.setImageURI(it)
                binding.btnTakePhoto.text = "Ubah Foto"
                Toast.makeText(requireContext(), "Foto profil berhasil diperbarui ✅", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                Toast.makeText(requireContext(), "Izin kamera diperlukan untuk ambil foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
