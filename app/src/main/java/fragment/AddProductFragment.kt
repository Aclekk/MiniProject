package com.example.miniproject.fragment

import android.os.Bundle
import android.util.Log // <-- TAMBAHKAN IMPORT INI
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.MainActivity
import com.example.miniproject.R
import com.example.miniproject.api.ApiClient
import com.example.miniproject.databinding.FragmentAddProductBinding
import com.example.miniproject.model.Category
import com.example.miniproject.model.CategoryResponse
import com.example.miniproject.model.ProductRequest
import com.example.miniproject.model.ProductResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val categories = mutableListOf<Category>()

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
        loadCategories()
        setupClickListeners()
    }

    private fun loadCategories() {
        ApiClient.apiService.getAllCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { categoryList ->
                        categories.clear()
                        categories.addAll(categoryList)
                        // Log untuk melihat kategori yang dimuat
                        Log.d("AddProductFragment", "Categories loaded: $categories")

                        val categoryNames = categories.map { it.categoryName }
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            categoryNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerCategory.adapter = adapter
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSaveProduct.setOnClickListener { saveProduct() }
        binding.btnCancel.setOnClickListener { goBackToProducts() }
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val imageUrl = binding.etProductImageUrl.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()
        val selectedCategoryPos = binding.spinnerCategory.selectedItemPosition

        if (!validateInput(name, priceStr, stockStr, selectedCategoryPos)) return

        val price = priceStr.toDouble()
        val stock = stockStr.toInt()
        
        // Ambil kategori yang dipilih
        val selectedCategory = categories.getOrNull(selectedCategoryPos)
        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Selected category not found.", Toast.LENGTH_SHORT).show()
            // Log jika kategori yang dipilih tidak ditemukan di list 'categories'
            Log.w("AddProductFragment", "Selected category at position $selectedCategoryPos is null. Spinner count: ${binding.spinnerCategory.count}, Categories list size: ${categories.size}")
            return
        }
        val categoryId = selectedCategory.id

        // Log categoryId yang akan dikirim
        Log.d("AddProductFragment", "Attempting to save product with categoryId: $categoryId (Selected: ${selectedCategory.categoryName}, Position: $selectedCategoryPos)")

        val productRequest = ProductRequest(
            name = name,
            price = price,
            description = description,
            imageUrl = imageUrl.ifEmpty { "https://via.placeholder.com/300x200?text=Product" },
            categoryId = categoryId,
            stock = stock
        )

        showLoading(true)

        ApiClient.apiService.createProduct(productRequest).enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                showLoading(false)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()
                    Log.d("AddProductFragment", "Product added successfully. Response: ${response.body()}")
                    goBackToProducts()
                } else {
                    val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Failed to add product"
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("AddProductFragment", "Failed to add product: $errorMsg. Response: ${response.raw()}")
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("AddProductFragment", "Network error on saveProduct: ${t.message}", t)
            }
        })
    }

    private fun validateInput(name: String, price: String, stock: String, categoryPos: Int): Boolean {
        return when {
            name.isEmpty() -> {
                binding.etProductName.error = "Name required"; false
            }
            price.isEmpty() -> {
                binding.etProductPrice.error = "Price required"; false
            }
            stock.isEmpty() -> {
                binding.etProductStock.error = "Stock required"; false
            }
            categoryPos < 0 -> { // AdapterView.INVALID_POSITION usually -1
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                Log.w("AddProductFragment", "Validation failed: No category selected (position: $categoryPos)")
                false
            }
            else -> {
                try {
                    price.toDouble(); stock.toInt(); true
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Invalid price or stock format", Toast.LENGTH_SHORT).show()
                    Log.w("AddProductFragment", "Validation failed: Invalid number format for price or stock.", e)
                    false
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveProduct.isEnabled = !show
    }

    private fun goBackToProducts() {
        // Toast message di sini sedikit membingungkan jika kategori tidak tersimpan dengan benar.
        // Mungkin lebih baik: "Product add attempt finished. Check Categories tab."
        Toast.makeText(requireContext(), "Product add attempt finished. Check Categories tab for updates.", Toast.LENGTH_LONG).show()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProductsFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
