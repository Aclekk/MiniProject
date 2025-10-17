package com.example.miniproject.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.ProductDataSource
import com.example.miniproject.databinding.FragmentHomeBinding
import com.example.miniproject.model.Category
import com.example.miniproject.model.Product
import com.google.android.material.slider.RangeSlider

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val displayProducts = mutableListOf<Product>() // data yang ditampilkan

    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategories()
        setupRecyclerView()
        loadProducts()
        setupSearch()
        setupFilter()
        setupFAB() //
    }
    private fun setupFAB() {
        val sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("role", "user") ?: "user"

        // Tampilkan FAB hanya untuk admin
        if (userRole == "admin") {
            binding.fabAddProduct.visibility = View.VISIBLE

            binding.fabAddProduct.setOnClickListener {
                val fragment = AddProductFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            binding.fabAddProduct.visibility = View.GONE
        }
    }
    override fun onResume() {
        super.onResume()
        // Refresh data setiap kali fragment visible
        refreshProducts()
    }

    private fun setupCategories() {
        categoryAdapter = CategoryAdapter(categories) { category ->
            filterProductsByCategory(category.categoryName)
        }

        val fromRepo = CategoryRepository.getCategories()
        categories.clear()
        categories.addAll(fromRepo)
        categoryAdapter.notifyDataSetChanged()
    }

    private fun filterProductsByCategory(name: String) {
        val allProducts = ProductDataSource.getAllProducts()
        val filtered = allProducts.filter { it.categoryName.equals(name, ignoreCase = true) }
        displayProducts.clear()
        displayProducts.addAll(filtered)
        productAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        val sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("role", "user") ?: "user"

        Log.d("HomeFragment", "👤 User Role: $userRole")

        productAdapter = ProductAdapter(displayProducts, userRole) { product, action ->
            when (action) {
                "edit" -> {
                    Log.d("HomeFragment", "✏️ Edit product: ${product.name}")
                    val bundle = Bundle().apply { putParcelable("product", product) }
                    val fragment = EditProductFragment().apply { arguments = bundle }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                "delete" -> {
                    Log.d("HomeFragment", "🗑️ Delete product: ${product.name}")
                    AlertDialog.Builder(requireContext())
                        .setTitle("Hapus Produk")
                        .setMessage("Yakin ingin menghapus ${product.name}?")
                        .setPositiveButton("Hapus") { dialog, _ ->
                            ProductDataSource.deleteProduct(product)
                            refreshProducts()
                            Toast.makeText(context, "✅ Produk dihapus", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Batal") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
                "view" -> {
                    val bundle = Bundle().apply { putParcelable("product", product) }
                    val fragment = ProductDetailFragment().apply { arguments = bundle }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        binding.rvHomeProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
            clipToPadding = false
            isNestedScrollingEnabled = false
        }
    }

    private fun loadProducts() {
        Log.d("HomeFragment", "📦 Memuat produk...")

        // Load dummy data (hanya sekali saat app start)
        ProductDataSource.loadDummyData()

        refreshProducts()
    }

    private fun refreshProducts() {
        val allProducts = ProductDataSource.getAllProducts()
        displayProducts.clear()
        displayProducts.addAll(allProducts)

        Log.d("HomeFragment", "✅ Produk dimuat: ${displayProducts.size}")
        productAdapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        binding.etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString().lowercase().trim()
                val allProducts = ProductDataSource.getAllProducts()

                val filtered = if (text.isEmpty()) {
                    allProducts.toList()
                } else {
                    allProducts.filter {
                        it.name.lowercase().contains(text) ||
                                it.categoryName?.lowercase()?.contains(text) == true ||
                                it.description?.lowercase()?.contains(text) == true
                    }
                }

                displayProducts.clear()
                displayProducts.addAll(filtered)
                productAdapter.notifyDataSetChanged()

                Log.d("HomeFragment", "🔎 Search: \"$text\" → ${filtered.size} hasil")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilter() {
        binding.btnFilterHome.setOnClickListener {
            Log.d("HomeFragment", "⚙️ Tombol Filter diklik")

            val sliderView = layoutInflater.inflate(R.layout.dialog_price_filter, null)
            val slider = sliderView.findViewById<RangeSlider>(R.id.sliderPrice)

            val allProducts = ProductDataSource.getAllProducts()
            val minPrice = allProducts.minOfOrNull { it.price } ?: 0.0
            val maxPrice = allProducts.maxOfOrNull { it.price } ?: 10000000.0

            slider.valueFrom = minPrice.toFloat()
            slider.valueTo = maxPrice.toFloat()
            slider.setValues(minPrice.toFloat(), maxPrice.toFloat())

            slider.addOnChangeListener { _, _, _ ->
                val values = slider.values
                Log.d("HomeFragment", "💰 Range slider: ${values[0]} - ${values[1]}")
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Filter Harga")
                .setView(sliderView)
                .setPositiveButton("Terapkan") { dialog, _ ->
                    val values = slider.values
                    val minPriceVal = values[0].toInt()
                    val maxPriceVal = values[1].toInt()

                    val filtered = allProducts.filter {
                        it.price.toInt() in minPriceVal..maxPriceVal
                    }

                    displayProducts.clear()
                    displayProducts.addAll(filtered)
                    productAdapter.notifyDataSetChanged()

                    Log.d("HomeFragment", "✅ Filter harga diterapkan: $minPriceVal - $maxPriceVal → ${filtered.size} hasil")
                    dialog.dismiss()
                }
                .setNegativeButton("Tampilkan Semua") { dialog, _ ->
                    displayProducts.clear()
                    displayProducts.addAll(allProducts)
                    productAdapter.notifyDataSetChanged()

                    Log.d("HomeFragment", "🔁 Filter direset → ${allProducts.size} hasil")
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("HomeFragment", "🧹 onDestroyView dipanggil")
    }
}