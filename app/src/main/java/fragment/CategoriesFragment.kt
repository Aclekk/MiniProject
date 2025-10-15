package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.CategoryRepository
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

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        getUserData()
        setupRecyclerViews()
        setupClickListeners()
        loadDummyCategories()
        loadDummyProducts()

        val argId = arguments?.getInt("category_id", -1) ?: -1
        val argName = arguments?.getString("category_name")

        if (argId != -1 || !argName.isNullOrBlank()) {
            val preselect = categories.find { it.id == argId }
                ?: categories.find { it.categoryName.equals(argName, ignoreCase = true) }

            preselect?.let { cat ->
                view.post {
                    showProductsForCategory(cat)
                    val idx = categories.indexOf(cat)
                    if (idx >= 0) binding.rvCategories.scrollToPosition(idx)
                }
            }
        }
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
        categoryAdapter = CategoryAdapter(categories) { category ->
            showProductsForCategory(category)
        }
        binding.rvCategories.adapter = categoryAdapter

        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "view" -> Toast.makeText(requireContext(), "View: ${product.name}", Toast.LENGTH_SHORT).show()
                "edit" -> Toast.makeText(requireContext(), "Edit: ${product.name}", Toast.LENGTH_SHORT).show()
                "delete" -> Toast.makeText(requireContext(), "Delete: ${product.name}", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun loadDummyCategories() {
        val repoCategories = CategoryRepository.getCategories()

        categories.clear()
        categories.addAll(repoCategories)
        categoryAdapter.notifyDataSetChanged()
    }

    private fun loadDummyProducts() {
        val dummyProducts = listOf(
            Product(
                id = 1, name = "Cangkul Premium", price = 150000.0, description = "Cangkul berkualitas tinggi.", categoryId = 1, stock = 50,
                categoryName = "Peralatan", imageResId = R.drawable.cangkul, imageUrl = null, createdAt = null
            ),
            Product(
                id = 2, name = "Pupuk Organik 25kg", price = 200000.0, description = "Pupuk untuk semua jenis tanaman.", categoryId = 2, stock = 30,
                categoryName = "Pupuk", imageResId = R.drawable.pupuk, imageUrl = null, createdAt = null
            ),
            Product(
                id = 3, name = "Benih Padi Premium", price = 50000.0, description = "Benih padi kualitas unggul.", categoryId = 3, stock = 100,
                categoryName = "Benih", imageResId = R.drawable.benih, imageUrl = null, createdAt = null
            ),
            Product(
                id = 4, name = "Traktor Mini", price = 5000000.0, description = "Traktor untuk sawah luas.", categoryId = 4, stock = 5,
                categoryName = "Alat Pertanian", imageResId = R.drawable.traktor, imageUrl = null, createdAt = null
            ),
            Product(id = 5, name = "Pestisida Alami 500ml", price = 75000.0, description = "Pestisida dari bahan alami.", categoryId = 5, stock = 60, categoryName = "Pestisida", imageResId = null, imageUrl = null, createdAt = null),
            Product(id = 6, name = "Benih Jagung Hibrida", price = 45000.0, description = "Benih jagung hibrida F1.", categoryId = 3, stock = 80, categoryName = "Benih", imageResId = null, imageUrl = null, createdAt = null),
            Product(id = 7, name = "Pupuk NPK", price = 180000.0, description = "Pupuk NPK seimbang.", categoryId = 2, stock = 40, categoryName = "Pupuk", imageResId = null, imageUrl = null, createdAt = null),
            Product(id = 8, name = "Sekop Tani Kuat", price = 120000.0, description = "Sekop dari bahan baja.", categoryId = 1, stock = 25, categoryName = "Peralatan", imageResId = null, imageUrl = null, createdAt = null)
        )

        allProducts.clear()
        allProducts.addAll(dummyProducts)
    }

    private fun showProductsForCategory(category: Category) {
        binding.tvCategoryTitle.text = "Products in ${category.categoryName}"
        binding.llProductsSection.visibility = View.VISIBLE

        val filteredProducts = allProducts.filter { it.categoryId == category.id }

        products.clear()
        products.addAll(filteredProducts)
        productAdapter.notifyDataSetChanged()

        if (products.isEmpty()) {
            Toast.makeText(requireContext(), "No products found in ${category.categoryName}", Toast.LENGTH_SHORT).show()
        }

        // Scroll to the product list
        binding.nestedScrollView.post {
            binding.nestedScrollView.smoothScrollTo(0, binding.llProductsSection.top)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
