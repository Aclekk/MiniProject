package com.example.miniproject.fragment

import android.content.Context
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
import com.bumptech.glide.Glide
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentEditProductBinding
import com.example.miniproject.model.Product
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private lateinit var product: Product

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
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product = arguments?.getParcelable("product") ?: run {
            Toast.makeText(context, "❌ Error: Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        setupCategorySpinner()
        loadProductData()
        setupClickListeners()
    }

    // Helper biar gak nulis ulang
    private fun String.toPlainRequestBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())

    private fun setupCategorySpinner() {
        val categories = CategoryRepository.getCategories()
        val categoryNames = categories.map { it.categoryName }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categoryNames
        )
        binding.spinnerCategory.adapter = adapter

        val selectedIndex = categories.indexOfFirst { it.id == product.categoryId }
        if (selectedIndex >= 0) {
            binding.spinnerCategory.setSelection(selectedIndex)
        }
    }

    private fun loadProductData() {
        binding.etProductName.setText(product.name)
        binding.etProductPrice.setText(product.price.toString())
        binding.etProductDescription.setText(product.description)
        binding.etProductStock.setText(product.stock.toString())

        product.imageUrl?.let { imageUrl ->
            try {
                Glide.with(requireContext())
                    .load(ApiClient.getImageUrl(imageUrl))
                    .into(binding.imgPreview)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnUpdate.setOnClickListener {
            updateProduct()
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

    // UPDATE PRODUK SESUAI BACKEND (product_id di @Part, image optional)
    private fun updateProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()
        val categoryName = binding.spinnerCategory.selectedItem?.toString() ?: ""

        // Validasi basic
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

        // Ambil category ID
        val categories = CategoryRepository.getCategories()
        val selectedCategory = categories.find { it.categoryName == categoryName }
        val categoryId = selectedCategory?.id ?: product.categoryId ?: 1

        // Token
        val sharedPref = requireContext()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "❌ Token hilang, silakan login ulang", Toast.LENGTH_LONG).show()
            return
        }

        // RequestBody text
        val productIdBody = product.id.toString().toPlainRequestBody()
        val nameBody = name.toPlainRequestBody()
        val priceBody = price.toString().toPlainRequestBody()
        val stockBody = stock.toString().toPlainRequestBody()
        val descBody = description.toPlainRequestBody()
        val catBody = categoryId.toString().toPlainRequestBody()

        // IMAGE PART (optional)
        var imagePart: MultipartBody.Part? = null
        if (selectedImageUri != null) {
            try {
                val stream = requireContext().contentResolver
                    .openInputStream(selectedImageUri!!)
                val bytes = stream!!.readBytes()
                stream.close()

                val cr = requireContext().contentResolver
                val mimeType = cr.getType(selectedImageUri!!) ?: "image/jpeg"   // ambil MIME asli (jpeg/png/webp)

                val reqFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData(
                    "image",
                    "product_${product.id}.jpg",
                    reqFile
                )

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "❌ Error membaca gambar: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // CALL API
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.updateProduct(
                    token = "Bearer $token",
                    productId = productIdBody,
                    name = nameBody,
                    categoryId = catBody,
                    price = priceBody,
                    stock = stockBody,
                    description = descBody,
                    image = imagePart
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(
                            requireContext(),
                            "✅ Produk berhasil diupdate!",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "❌ Update gagal: ${body?.message ?: "unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val errorText = response.errorBody()?.string()
                    Log.e("EditProduct", "Update failed: code=${response.code()} body=$errorText")
                    Toast.makeText(
                        requireContext(),
                        "❌ Gagal update (${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("EditProduct", "Exception: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
