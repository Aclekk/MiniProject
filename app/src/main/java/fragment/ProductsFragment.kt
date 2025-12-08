package com.example.miniproject.fragment

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
import com.bumptech.glide.Glide
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
    private var userRole: String = "buyer"

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
        loadBestSellerProducts()
        setupCategories()

        // 🔥 Load info toko (nama, tagline, footer, logo)
        loadStoreInfo()
    }

    override fun onResume() {
        super.onResume()
        // Refresh produk & info toko ketika balik ke fragment
        loadBestSellerProducts()
        loadStoreInfo()
    }

    // ================== USER DATA / ROLE ==================

    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "buyer") ?: "buyer"

        // FAB dimatikan untuk semua role di halaman ini
        binding.fabAddProduct.visibility = View.GONE
    }

    // ================== RECYCLER VIEW ==================

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
        // TODO: pasang adapter kategori kalau mau
        // binding.rvCategories.adapter = ...
    }

    // ================== CLICK LISTENERS ==================

    private fun setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadBestSellerProducts()
            loadStoreInfo()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnFilter.setOnClickListener {
            Toast.makeText(context, "Filter coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    // ================== SEARCH ==================

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

    // ================== LOAD BEST SELLERS ==================

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

    // ================== LOAD STORE INFO (HEADER + FOOTER) ==================

    private fun loadStoreInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getSettings()

                if (response.isSuccessful && response.body()?.success == true) {
                    // Sama seperti di ProfileFragment: data = Map<String, Any>
                    val settings = response.body()?.data as? Map<String, Any> ?: emptyMap()

                    val appName = settings["app_name"]?.toString().orEmpty()
                    val appTagline = settings["app_tagline"]?.toString().orEmpty()
                    val contactEmail = settings["contact_email"]?.toString().orEmpty()
                    val contactPhone = settings["contact_phone"]?.toString().orEmpty()
                    val appAddress = settings["app_address"]?.toString().orEmpty()
                    val logoPath = settings["app_logo"]?.toString()

                    // 🔹 HEADER (atas)
                    binding.tvStoreName.text =
                        if (appName.isNotBlank()) appName else "🌾 Niaga Tani"

                    binding.tvStoreTagline.text =
                        if (appTagline.isNotBlank()) appTagline else "Solusi Pertanian Modern Indonesia"

                    // 🔹 FOOTER (Tentang kami)
                    if (appTagline.isNotBlank()) {
                        binding.tvAboutTagline.text = appTagline
                    }

                    if (appAddress.isNotBlank()) {
                        binding.tvAboutAddress.text = "📍 $appAddress"
                    }

                    if (contactPhone.isNotBlank()) {
                        binding.tvAboutPhone.text = "📞 $contactPhone"
                    }

                    if (contactEmail.isNotBlank()) {
                        binding.tvAboutEmail.text = "📧 $contactEmail"
                    }

                    // 🔹 Logo toko
                    if (!logoPath.isNullOrEmpty()) {
                        val logoUrl = ApiClient.getImageUrl(logoPath)
                        Glide.with(this@ProductsFragment)
                            .load(logoUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(binding.imgStoreLogo)
                    }
                }
            } catch (e: Exception) {
                // Jangan spam toast di home, cukup diam kalau gagal
            }
        }
    }

    // ================== CLEANUP ==================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
