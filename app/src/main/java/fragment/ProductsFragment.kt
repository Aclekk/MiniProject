package com.example.miniproject.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentProductsBinding
import com.example.miniproject.model.Product
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()
    private var userRole = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUserData()
        setupRecyclerView()
        setupClickListeners()
        setupSearch()
        loadBestSellerProducts() // ✅ GANTI: Load best sellers aja
        setupCategories()
    }

    override fun onResume() {
        super.onResume()
        loadBestSellerProducts() // ✅ Refresh best sellers
    }

    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "buyer") ?: "buyer"

        // ❌ FAB DIMATIKAN UNTUK SEMUA ROLE
        binding.fabAddProduct.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "view" -> {
                    val bundle = Bundle().apply {
                        putParcelable("product", product)
                    }
                    val fragment = ProductDetailFragment().apply {
                        arguments = bundle
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun setupCategories() {
        val categories = CategoryRepository.getCategories()
        // TODO: setup adapter kategori kalau mau dipakai
        // binding.rvCategories.adapter = ...
    }

    private fun setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadBestSellerProducts()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnFilter.setOnClickListener {
            Toast.makeText(context, "Filter coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterProducts(s.toString())
            }
        })
    }

    private fun filterProducts(query: String) {
        val filtered = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.description?.contains(query, ignoreCase = true) == true ||
                        product.categoryName?.contains(query, ignoreCase = true) == true
            }
        }

        products.clear()
        products.addAll(filtered)
        productAdapter.notifyDataSetChanged()

        if (filtered.isEmpty()) {
            Toast.makeText(context, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ LOAD BEST SELLER PRODUCTS (untuk customer di Home)
    private fun loadBestSellerProducts() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getBestSellerProducts()

                if (response.isSuccessful) {
                    val body = response.body()
                    val list = body?.data?.products ?: emptyList()

                    allProducts.clear()
                    allProducts.addAll(list)

                    products.clear()
                    products.addAll(list)

                    productAdapter.notifyDataSetChanged()

                    if (list.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Belum ada produk terlaris",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}