package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.api.ApiClient
import com.example.miniproject.databinding.FragmentCategoriesBinding // Added ViewBinding import
import com.example.miniproject.model.Category
import com.example.miniproject.model.CategoryResponse
import com.example.miniproject.model.Product
import com.example.miniproject.model.ProductResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null // Added ViewBinding
    private val binding get() = _binding!! // Added ViewBinding

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    private val categories = mutableListOf<Category>()
    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()

    private var userRole = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false) // Changed to ViewBinding
        return binding.root // Changed to ViewBinding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initViews(view) // No longer needed with ViewBinding
        getUserData()
        setupRecyclerViews()
        setupClickListeners() // Added call to setupClickListeners
        loadCategories()
        loadAllProducts()
    }

    // initViews is no longer needed with ViewBinding

    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "user") ?: "user"

        if (userRole == "admin") {
            binding.fabAddCategory.visibility = View.VISIBLE
        } else {
            binding.fabAddCategory.visibility = View.GONE
        }
    }

    private fun setupRecyclerViews() {
        // Categories RecyclerView - Grid
        categoryAdapter = CategoryAdapter(categories) { category ->
            showProductsForCategory(category)
        }
        binding.rvCategories.apply { // Changed to use binding
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
        }

        // Products RecyclerView - Linear
        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "view" -> Toast.makeText(requireContext(), "View: ${product.name}", Toast.LENGTH_SHORT).show()
                "edit" -> Toast.makeText(requireContext(), "Edit: ${product.name}", Toast.LENGTH_SHORT).show()
                "delete" -> Toast.makeText(requireContext(), "Delete: ${product.name}", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvCategoryProducts.apply { // Changed to use binding
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupClickListeners() { // Added this method
        binding.fabAddCategory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddCategoryFragment()) 
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadCategories() {
        showLoading(true)
        ApiClient.apiService.getAllCategories().enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                showLoading(false)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { categoryList ->
                        categories.clear()
                        categories.addAll(categoryList)
                        categoryAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load categories: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAllProducts() {
        val call = ApiClient.apiService.getAllProducts()
        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { productList ->
                        allProducts.clear()
                        allProducts.addAll(productList)
                    }
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                // Silent fail for all products load
            }
        })
    }

    private fun showProductsForCategory(category: Category) {
        binding.tvCategoryTitle.text = "Products in ${category.categoryName}" // Changed to use binding
        binding.llProductsSection.visibility = View.VISIBLE // Changed to use binding

        val filteredProducts = allProducts.filter { product ->
            product.categoryName?.equals(category.categoryName, ignoreCase = true) == true || product.categoryId == category.id
        }

        products.clear()
        products.addAll(filteredProducts)
        productAdapter.notifyDataSetChanged()

        if (products.isEmpty()) {
            Toast.makeText(requireContext(), "No products found in ${category.categoryName}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE // Changed to use binding
    }

    override fun onDestroyView() { // Added onDestroyView for ViewBinding
        super.onDestroyView()
        _binding = null
    }
}