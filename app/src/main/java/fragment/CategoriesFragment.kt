package com.example.miniproject.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.ProductDataSource
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentCategoriesBinding
import com.example.miniproject.model.Category
import com.example.miniproject.model.Product
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter

    private val categories = mutableListOf<Category>()
    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()

    private var userRole = ""

    // ✅ Flag untuk track loading state
    private var categoriesLoaded = false
    private var productsLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = ""

        getUserData()
        setupRecyclerViews()
        setupClickListeners()

        // ✅ LOAD DATA - products dan categories parallel
        loadCategoriesAndProducts()
    }

    override fun onResume() {
        super.onResume()
        // Reset flags
        categoriesLoaded = false
        productsLoaded = false
        loadCategoriesAndProducts()
    }

    // =========================
    // USER / ROLE
    // =========================
    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "buyer") ?: "buyer"

        // ✅ FAB hanya untuk seller
        if (userRole == "seller") {
            binding.fabAddCategory.visibility = View.VISIBLE
        } else {
            binding.fabAddCategory.visibility = View.GONE
        }
    }

    // =========================
    // RECYCLER SETUP
    // =========================
    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter(
            categories = categories,
            userRole = userRole,
            onItemClick = { category ->
                showProductsForCategory(category)
            },
            onEditClick = { category ->
                editCategory(category)
            },
            onDeleteClick = { category ->
                deleteCategory(category)
            }
        )

        binding.rvCategories.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCategories.adapter = categoryAdapter

        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "view" -> {
                    val bundle = Bundle().apply { putParcelable("product", product) }
                    val fragment = ProductDetailFragment().apply { arguments = bundle }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                "edit" -> {
                    val bundle = Bundle().apply { putParcelable("product", product) }
                    val fragment = EditProductFragment().apply { arguments = bundle }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                "delete" -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Hapus Produk")
                        .setMessage("Yakin ingin menghapus ${product.name}?")
                        .setPositiveButton("Hapus") { dialog, _ ->
                            val success = ProductDataSource.deleteProduct(product)
                            if (success) {
                                Toast.makeText(context, "✅ Produk dihapus", Toast.LENGTH_SHORT)
                                    .show()
                                productsLoaded = false
                                loadCategoriesAndProducts()
                                categories.find { it.id == product.categoryId }?.let {
                                    showProductsForCategory(it)
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "❌ Gagal menghapus produk",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("Batal") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }

        binding.rvCategoryProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryProducts.adapter = productAdapter
    }

    private fun setupClickListeners() {
        binding.fabAddCategory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddCategoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // =========================
    // ✅ LOAD CATEGORIES & PRODUCTS BERSAMAAN
    // =========================
    private fun loadCategoriesAndProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Launch both in parallel
            launch { loadCategories() }
            launch { loadProducts() }
        }
    }

    // =========================
    // LOAD CATEGORIES DARI API
    // =========================
    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repoCategories = CategoryRepository.getCategories()

            categories.clear()
            categories.addAll(repoCategories)
            categoryAdapter.notifyDataSetChanged()

            categoriesLoaded = true

            android.util.Log.d("CategoriesFragment", "✅ Categories loaded: ${categories.size}")

            // ✅ CEK APAKAH SUDAH BISA HANDLE ARGUMENTS
            checkAndHandleArguments()
        }
    }

    // =========================
    // LOAD PRODUCTS VIA API
    // =========================
    private fun loadProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getProducts(
                    page = 1,
                    limit = 200,
                    categoryId = null,
                    search = null,
                    minPrice = null,
                    maxPrice = null
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()
                    val list = body?.data?.products ?: emptyList()

                    allProducts.clear()
                    allProducts.addAll(list)

                    productsLoaded = true

                    android.util.Log.d("CategoriesFragment", "✅ Products loaded: ${allProducts.size}")

                    // ✅ CEK APAKAH SUDAH BISA HANDLE ARGUMENTS
                    checkAndHandleArguments()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal load produk: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek server: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // =========================
    // ✅ CEK & HANDLE ARGUMENTS (HANYA JIKA KEDUA DATA SUDAH LOADED)
    // =========================
    private fun checkAndHandleArguments() {
        // ✅ TUNGGU SAMPAI CATEGORIES DAN PRODUCTS SELESAI DIMUAT
        if (!categoriesLoaded || !productsLoaded) {
            android.util.Log.d("CategoriesFragment", "⏳ Waiting... Categories: $categoriesLoaded, Products: $productsLoaded")
            return
        }

        if (categories.isEmpty() || allProducts.isEmpty()) {
            android.util.Log.d("CategoriesFragment", "⚠️ Data empty. Categories: ${categories.size}, Products: ${allProducts.size}")
            return
        }

        val argId = arguments?.getInt("category_id", -1) ?: -1
        val argName = arguments?.getString("category_name")

        android.util.Log.d("CategoriesFragment", "🔍 Arguments - ID: $argId, Name: $argName")

        // Cek apakah ada argumen yang valid
        if (argId != -1 || !argName.isNullOrBlank()) {
            val preselect = categories.find { it.id == argId }
                ?: categories.find { it.categoryName.equals(argName, ignoreCase = true) }

            if (preselect != null) {
                android.util.Log.d("CategoriesFragment", "✅ Found category: ${preselect.categoryName} (ID: ${preselect.id})")

                // Tunggu view selesai render
                view?.post {
                    showProductsForCategory(preselect)

                    // Scroll ke kategori yang dipilih
                    val idx = categories.indexOf(preselect)
                    if (idx >= 0) {
                        binding.rvCategories.scrollToPosition(idx)
                    }
                }

                // ✅ CLEAR ARGUMENTS setelah diproses
                arguments?.clear()
            } else {
                android.util.Log.d("CategoriesFragment", "❌ Category not found")
            }
        }
    }

    // =========================
    // FILTER & TAMPILKAN PER KATEGORI
    // =========================
    private fun showProductsForCategory(category: Category) {
        android.util.Log.d("CategoriesFragment", "🔍 Filtering products for category: ${category.categoryName} (ID: ${category.id})")
        android.util.Log.d("CategoriesFragment", "📦 Total products available: ${allProducts.size}")

        binding.tvCategoryTitle.text = "Produk di ${category.categoryName}"
        binding.llProductsSection.visibility = View.VISIBLE

        val filteredProducts = allProducts.filter { it.categoryId == category.id }

        android.util.Log.d("CategoriesFragment", "✅ Filtered products: ${filteredProducts.size}")
        filteredProducts.forEach {
            android.util.Log.d("CategoriesFragment", "  - ${it.name} (CategoryID: ${it.categoryId})")
        }

        products.clear()
        products.addAll(filteredProducts)
        productAdapter.notifyDataSetChanged()

        if (products.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Belum ada produk di ${category.categoryName}",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ✅ Smooth scroll ke section produk
        binding.nestedScrollView.post {
            binding.nestedScrollView.smoothScrollTo(0, binding.llProductsSection.top)
        }
    }

    private fun editCategory(category: Category) {
        val fragment = EditCategoryFragment().apply {
            arguments = Bundle().apply {
                putInt("category_id", category.id)
                putString("category_name", category.categoryName)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // =========================
    // DELETE CATEGORY VIA API
    // =========================
    private fun deleteCategory(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kategori")
            .setMessage("Yakin ingin menghapus kategori '${category.categoryName}'? Semua produk dalam kategori ini juga akan terhapus.")
            .setPositiveButton("Hapus") { dialog, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val success = CategoryRepository.deleteCategory(category.id)
                    if (success) {
                        Toast.makeText(context, "✅ Kategori dihapus", Toast.LENGTH_SHORT).show()
                        categoriesLoaded = false
                        productsLoaded = false
                        loadCategoriesAndProducts()
                        binding.llProductsSection.visibility = View.GONE
                    } else {
                        Toast.makeText(
                            context,
                            "❌ Gagal menghapus kategori",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}