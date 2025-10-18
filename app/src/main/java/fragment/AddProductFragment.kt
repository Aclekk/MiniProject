package com.example.miniproject.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.ProductDataSource
import com.example.miniproject.databinding.FragmentAddProductBinding
import com.example.miniproject.model.Product
import java.text.SimpleDateFormat
import java.util.*

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    // 📸 Tambahan variabel untuk simpan foto yang dipilih
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 100

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

        // 📸 Tambahan: tombol pilih foto
        binding.btnPickImage.setOnClickListener {
            openGallery()
        }
    }

    // ============================================================
    // 📷 BUKA GALERI
    // ============================================================
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // 📸 Ambil hasil dari galeri dan tampilkan di ImageView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.imgPreview.setImageURI(selectedImageUri)
        }
    }

    // ============================================================
    // 💾 SIMPAN PRODUK (dummy flow tetap)
    // ============================================================
    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()
        val categoryName = binding.spinnerCategory.selectedItem.toString()

        // Validasi
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

        // Generate ID baru (auto-increment)
        val allProducts = ProductDataSource.getAllProducts()
        val newId = if (allProducts.isEmpty()) 1 else (allProducts.maxOf { it.id } + 1)

        // Get category ID
        val categories = CategoryRepository.getCategories()
        val selectedCategory = categories.find { it.categoryName == categoryName }
        val categoryId = selectedCategory?.id ?: 1

        // 🧩 Buat produk baru (dummy save)
        val newProduct = Product(
            id = newId,
            name = name,
            price = priceStr.toDouble(),
            description = description,
            imageUrl = selectedImageUri?.toString(), // ✅ simpan URI foto kalau ada
            imageResId = getDefaultImageForCategory(categoryName),
            categoryId = categoryId,
            stock = stockStr.toInt(),
            categoryName = categoryName,
            createdAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        // Tambahkan ke data dummy (tidak permanen)
        ProductDataSource.getAllProducts().add(newProduct)

        Toast.makeText(context, "✅ Produk berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun getDefaultImageForCategory(categoryName: String): Int {
        return when (categoryName) {
            "Peralatan" -> R.drawable.cangkul
            "Pupuk" -> R.drawable.pupuk
            "Benih" -> R.drawable.benih
            else -> R.drawable.bg_card
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
