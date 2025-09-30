package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.api.ApiClient
import com.example.miniproject.model.Category
// import com.example.miniproject.model.CategoryRequest // Dihapus karena tidak dipakai
import com.example.miniproject.model.CategoryResponse
import com.example.miniproject.model.Product
import com.example.miniproject.model.ProductResponse
// import com.google.android.material.floatingactionbutton.FloatingActionButton // Dihapus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesFragment : Fragment() {

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvCategoryProducts: RecyclerView
    private lateinit var tvCategoryTitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var llProductsSection: View
    // private lateinit var fabAddCategory: FloatingActionButton // Dihapus

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
    ): View? {
        // Asumsi: Kamu juga sudah menghapus FAB dari file layout 'fragment_categories.xml'
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        getUserData()
        setupRecyclerViews()
        loadCategories()
        loadAllProducts()

        // Listener untuk fabAddCategory sudah Dihapus
    }

    private fun initViews(view: View) {
        rvCategories = view.findViewById(R.id.rvCategories)
        rvCategoryProducts = view.findViewById(R.id.rvCategoryProducts)
        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle)
        progressBar = view.findViewById(R.id.progressBar)
        llProductsSection = view.findViewById(R.id.llProductsSection)
        // fabAddCategory = view.findViewById(R.id.fabAddCategory) // Baris ini Dihapus
    }

    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "user") ?: "user"
    }

    private fun setupRecyclerViews() {
        // Categories RecyclerView - Grid
        categoryAdapter = CategoryAdapter(categories) { category ->
            showProductsForCategory(category)
        }
        rvCategories.apply {
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
        rvCategoryProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
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

    // Fungsi showAddCategoryDialog() sudah Dihapus
    // Fungsi addCategoryToApi() sudah Dihapus

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
        tvCategoryTitle.text = "Products in ${category.categoryName}"
        llProductsSection.visibility = View.VISIBLE

        val filteredProducts = allProducts.filter { product ->
            // Logika filter produk berdasarkan kategori (sesuaikan jika perlu)
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
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}