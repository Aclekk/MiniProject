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
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.databinding.FragmentCategoriesBinding
import com.example.miniproject.model.Category
import com.example.miniproject.model.Product

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUserData()
        setupRecyclerViews()
        setupClickListeners()
        loadDummyCategories()
        loadDummyProducts()
    }

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
        binding.rvCategories.apply {
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
        binding.rvCategoryProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddCategory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddCategoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadDummyCategories() {
        // DUMMY DATA - Kategori
        val dummyCategories = listOf(
            Category(1, "Pertanian", "2025-01-01"),
            Category(2, "Pupuk", "2025-01-01"),
            Category(3, "Benih", "2025-01-01"),
            Category(4, "Peralatan", "2025-01-01"),
            Category(5, "Pestisida", "2025-01-01")
        )

        categories.clear()
        categories.addAll(dummyCategories)
        categoryAdapter.notifyDataSetChanged()
    }

    private fun loadDummyProducts() {
        // DUMMY DATA - Produk
        val dummyProducts = listOf(
            Product(
                id = 1,
                name = "Cangkul Premium",
                price = 150000.0,
                description = "Cangkul berkualitas tinggi untuk mengolah tanah",
                imageUrl = "https://via.placeholder.com/300x200?text=Cangkul",
                categoryId = 1,
                stock = 50,
                categoryName = "Pertanian",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 2,
                name = "Pupuk Organik 25kg",
                price = 200000.0,
                description = "Pupuk organik alami berkualitas tinggi",
                imageUrl = "https://via.placeholder.com/300x200?text=Pupuk",
                categoryId = 2,
                stock = 30,
                categoryName = "Pupuk",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 3,
                name = "Benih Padi Premium",
                price = 50000.0,
                description = "Benih padi unggul hasil seleksi",
                imageUrl = "https://via.placeholder.com/300x200?text=Benih",
                categoryId = 3,
                stock = 100,
                categoryName = "Benih",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 4,
                name = "Traktor Mini",
                price = 5000000.0,
                description = "Traktor mini untuk pertanian skala kecil",
                imageUrl = "https://via.placeholder.com/300x200?text=Traktor",
                categoryId = 4,
                stock = 5,
                categoryName = "Peralatan",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 5,
                name = "Pestisida Alami 500ml",
                price = 75000.0,
                description = "Pestisida ramah lingkungan",
                imageUrl = "https://via.placeholder.com/300x200?text=Pestisida",
                categoryId = 5,
                stock = 60,
                categoryName = "Pestisida",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 6,
                name = "Benih Jagung Hibrida",
                price = 45000.0,
                description = "Benih jagung hibrida tahan hama",
                imageUrl = "https://via.placeholder.com/300x200?text=Jagung",
                categoryId = 3,
                stock = 80,
                categoryName = "Benih",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 7,
                name = "Pupuk NPK",
                price = 180000.0,
                description = "Pupuk NPK lengkap untuk berbagai tanaman",
                imageUrl = "https://via.placeholder.com/300x200?text=NPK",
                categoryId = 2,
                stock = 40,
                categoryName = "Pupuk",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 8,
                name = "Sekop Tani Kuat",
                price = 120000.0,
                description = "Sekop berkualitas dengan gagang besi",
                imageUrl = "https://via.placeholder.com/300x200?text=Sekop",
                categoryId = 1,
                stock = 25,
                categoryName = "Pertanian",
                createdAt = "2025-01-01"
            )
        )

        allProducts.clear()
        allProducts.addAll(dummyProducts)
    }

    private fun showProductsForCategory(category: Category) {
        binding.tvCategoryTitle.text = "Products in ${category.categoryName}"
        binding.llProductsSection.visibility = View.VISIBLE

        val filteredProducts = allProducts.filter { product ->
            product.categoryName?.equals(category.categoryName, ignoreCase = true) == true
        }

        products.clear()
        products.addAll(filteredProducts)
        productAdapter.notifyDataSetChanged()

        if (products.isEmpty()) {
            Toast.makeText(requireContext(), "No products found in ${category.categoryName}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}