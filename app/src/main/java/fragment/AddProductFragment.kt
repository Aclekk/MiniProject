package com.example.miniproject.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.Category // ⬅️ SESUAIKAN
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

    private val categories = mutableListOf<Category>()
    private var selectedCategoryId: Int? = null

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
        loadCategoriesFromApi()
        setupClickListeners()
    }

    // ================== LOAD CATEGORIES DARI API ==================

    private fun loadCategoriesFromApi() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data   // <- nullable
                    val list = data?.categories ?: emptyList()  // <- SAFE ✅

                    categories.clear()
                    categories.addAll(list)

                    val names = categories.map { it.name } // atau it.categoryName, sesuaikan
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        names
                    )
                    binding.spinnerCategory.adapter = adapter

                    if (categories.isNotEmpty()) {
                        selectedCategoryId = categories[0].id
                    }

                    binding.spinnerCategory.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                selectedCategoryId = categories[position].id
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // no-op
                            }
                        }

                } else {
                    Toast.makeText(requireContext(), "Gagal load kategori", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ================== CLICK LISTENERS ==================

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener { saveProduct() }
        binding.btnCancel.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnPickImage.setOnClickListener { openGallery() }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    // ================== SAVE PRODUCT ==================

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()

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

        if (selectedCategoryId == null) {
            Toast.makeText(requireContext(), "Kategori belum dipilih", Toast.LENGTH_SHORT).show()
            return
        }

        val sp = requireActivity().getSharedPreferences("user_pref", android.content.Context.MODE_PRIVATE)
        val token = sp.getString("token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ada, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        val textMedia = "text/plain".toMediaType()

        val nameBody: RequestBody = name.toRequestBody(textMedia)
        val priceBody: RequestBody = price.toString().toRequestBody(textMedia)
        val stockBody: RequestBody = stock.toString().toRequestBody(textMedia)
        val descBody: RequestBody = description.toString().toRequestBody(textMedia)
        val categoryIdBody: RequestBody = selectedCategoryId.toString().toRequestBody(textMedia)

        var imagePart: MultipartBody.Part? = null
        if (selectedImageUri != null) {
            val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val imageRequestBody = bytes.toRequestBody("image/*".toMediaType())
                imagePart = MultipartBody.Part.createFormData(
                    "image",
                    "product_${System.currentTimeMillis()}.jpg",
                    imageRequestBody
                )
            }
        }

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

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "✅ Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
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
