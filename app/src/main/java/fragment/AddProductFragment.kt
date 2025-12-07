package com.example.miniproject.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentAddProductBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

    // ✅ Modern way untuk pick image
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.imgPreview.setImageURI(selectedImageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupClickListeners()
    }

    private fun setupCategorySpinner() {
        val categories = CategoryRepository.getCategories()
        val categoryNames = categories.map { it.categoryName }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryNames
        )
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveProduct()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnPickImage.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    // =======================
    // ✅ saveProduct FINAL (pakai API + Multipart)
    // =======================
    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()
        val categoryName = binding.spinnerCategory.selectedItem.toString()

        // ==== VALIDASI ====
        if (name.isEmpty()) {
            binding.etProductName.error = "Nama produk harus diisi"
            return
        }
        if (priceStr.isEmpty()) {
            binding.etProductPrice.error = "Harga harus diisi"
            return
        }
        if (stockStr.isEmpty()) {
            binding.etProductStock.error = "Stok harus diisi"
            return
        }

        val price = priceStr.toDoubleOrNull()
        val stock = stockStr.toIntOrNull()

        if (price == null || price <= 0) {
            binding.etProductPrice.error = "Harga tidak valid"
            return
        }
        if (stock == null || stock < 0) {
            binding.etProductStock.error = "Stok tidak valid"
            return
        }

        // Ambil category_id dari DB lokal (pastikan mapping-nya sama dengan backend)
        val categories = CategoryRepository.getCategories()
        val selectedCategory = categories.find { it.categoryName == categoryName }
        val categoryId = selectedCategory?.id ?: 1

        // Ambil token dari SharedPreferences
        val sp = requireActivity().getSharedPreferences("user_pref", android.content.Context.MODE_PRIVATE)
        val token = sp.getString("token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ada, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        // ==== KONVERSI KE RequestBody ====
        val textMedia = "text/plain".toMediaType()

        val nameBody: RequestBody = name.toRequestBody(textMedia)
        val priceBody: RequestBody = price.toString().toRequestBody(textMedia)
        val stockBody: RequestBody = stock.toString().toRequestBody(textMedia)
        val descBody: RequestBody = description.toString().toRequestBody(textMedia)
        val categoryIdBody: RequestBody = categoryId.toString().toRequestBody(textMedia)

        // ==== HANDLE IMAGE (optional) ====
        var imagePart: MultipartBody.Part? = null
        if (selectedImageUri != null) {
            val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val imageRequestBody = bytes.toRequestBody("image/*".toMediaType())
                imagePart = MultipartBody.Part.createFormData(
                    "image",   // ← HARUS SAMA DENGAN KEY di PHP ($_FILES['image'])
                    "product_${System.currentTimeMillis()}.jpg",
                    imageRequestBody
                )
            }
        }

        // ==== PANGGIL API ====
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.btnSave.isEnabled = false

                val response = ApiClient.apiService.createProduct(
                    "Bearer $token",
                    nameBody,
                    categoryIdBody,
                    priceBody,
                    stockBody,
                    descBody,
                    imagePart
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(requireContext(), "✅ Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal menambah produk",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}: ${response.errorBody()?.string()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek server: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnSave.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
