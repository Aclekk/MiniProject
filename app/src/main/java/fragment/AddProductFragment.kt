package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
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
                println("DEBUG: Response code: ${response.code()}")
                println("DEBUG: Response body: ${response.body()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { categoryList ->
                        println("DEBUG: Categories loaded: ${categoryList.size}")
                        categoryList.forEach { category ->
                            println("DEBUG: Category - ID: ${category.id}, Name: ${category.categoryName}")
                        }

                        categories.clear()
                        categories.addAll(categoryList)

                        val categoryNames = categories.map { it.categoryName }
                        println("DEBUG: Category names for spinner: $categoryNames")

                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerCategory.adapter = adapter
                    }
                } else {
                    println("DEBUG: Response failed - Success: ${response.body()?.success}")
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                println("DEBUG: API call failed: ${t.message}")
                Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSaveProduct.setOnClickListener {
            saveProduct()
        }

        binding.btnCancel.setOnClickListener {
            goBackToProducts()
        }
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val imageUrl = binding.etProductImageUrl.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()
        val selectedCategoryPos = binding.spinnerCategory.selectedItemPosition

        if (!validateInput(name, priceStr, stockStr, selectedCategoryPos)) {
            return
        }

        val price = priceStr.toDouble()
        val stock = stockStr.toInt()
        val categoryId = categories[selectedCategoryPos].id

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
                    goBackToProducts()
                } else {
                    Toast.makeText(requireContext(), response.body()?.message ?: "Failed to add product", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun validateInput(name: String, price: String, stock: String, categoryPos: Int): Boolean {
        return when {
            name.isEmpty() -> {
                binding.etProductName.error = "Name required"
                false
            }
            price.isEmpty() -> {
                binding.etProductPrice.error = "Price required"
                false
            }
            stock.isEmpty() -> {
                binding.etProductStock.error = "Stock required"
                false
            }
            categoryPos < 0 -> {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                false
            }
            else -> {
                try {
                    price.toDouble()
                    stock.toInt()
                    true
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Invalid price or stock format", Toast.LENGTH_SHORT).show()
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
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProductsFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}