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
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.ProductDataSource
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
                            ProductDataSource.deleteProduct(product)
                            loadDummyProducts()
                            showProductsForCategory(categories.find { it.id == product.categoryId }!!)
                            Toast.makeText(context, "✅ Produk dihapus", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Batal") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
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
        // ✅ Gunakan data dari ProductDataSource
        ProductDataSource.loadDummyData()

        val allData = ProductDataSource.getAllProducts()

        allProducts.clear()
        allProducts.addAll(allData)
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

        binding.nestedScrollView.post {
            binding.nestedScrollView.smoothScrollTo(0, binding.llProductsSection.top)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}