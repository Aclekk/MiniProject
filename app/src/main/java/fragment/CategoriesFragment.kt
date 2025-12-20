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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.repository.CategoryRepository
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
        loadCategoriesAndProducts()
    }

    override fun onResume() {
        super.onResume()
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

        binding.fabAddCategory.visibility =
            if (userRole == "seller") View.VISIBLE else View.GONE
    }

    // =========================
    // RECYCLER SETUP (FIX UTAMA DI SINI)
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

        // ✅ FIX FINAL: 1 BARIS = 1 CARD
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
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
                                Toast.makeText(context, "✅ Produk dihapus", Toast.LENGTH_SHORT).show()
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
    // LOAD DATA
    // =========================
    private fun loadCategoriesAndProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch { loadCategories() }
            launch { loadProducts() }
        }
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            val repoCategories = CategoryRepository.getCategories()
            categories.clear()
            categories.addAll(repoCategories)
            categoryAdapter.notifyDataSetChanged()
            categoriesLoaded = true
            checkAndHandleArguments()
        }
    }

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
                    val list = response.body()?.data?.products ?: emptyList()
                    allProducts.clear()
                    allProducts.addAll(list)
                    productsLoaded = true
                    checkAndHandleArguments()
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

    private fun checkAndHandleArguments() {
        if (!categoriesLoaded || !productsLoaded) return

        val argId = arguments?.getInt("category_id", -1) ?: -1
        val argName = arguments?.getString("category_name")

        val preselect = categories.find { it.id == argId }
            ?: categories.find { it.categoryName.equals(argName, true) }

        preselect?.let {
            view?.post {
                showProductsForCategory(it)
                binding.rvCategories.scrollToPosition(categories.indexOf(it))
            }
            arguments?.clear()
        }
    }

    private fun showProductsForCategory(category: Category) {
        binding.tvCategoryTitle.text = "Produk di ${category.categoryName}"
        binding.llProductsSection.visibility = View.VISIBLE

        val filtered = allProducts.filter { it.categoryId == category.id }
        products.clear()
        products.addAll(filtered)
        productAdapter.notifyDataSetChanged()

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

    private fun deleteCategory(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kategori")
            .setMessage("Yakin ingin menghapus kategori '${category.categoryName}'?")
            .setPositiveButton("Hapus") { dialog, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val success = CategoryRepository.deleteCategory(category.id)
                    if (success) {
                        Toast.makeText(context, "✅ Kategori dihapus", Toast.LENGTH_SHORT).show()
                        categoriesLoaded = false
                        productsLoaded = false
                        loadCategoriesAndProducts()
                        binding.llProductsSection.visibility = View.GONE
                    }
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
