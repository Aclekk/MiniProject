package com.example.miniproject.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentHomeBinding
import com.example.miniproject.ml.FarmToolClassifier
import com.example.miniproject.model.Product
import com.google.android.material.slider.RangeSlider
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var bestSellerAdapter: ProductAdapter

    private val displayProducts = mutableListOf<Product>()
    private val bestSellerProducts = mutableListOf<Product>()

    private var userRole = "user"

    // 🆕 ML Classifier
    private lateinit var farmToolClassifier: FarmToolClassifier

    // 🆕 Flag untuk skip onResume refresh setelah visual search
    private var isVisualSearchActive = false

    // 🆕 LOCK untuk prevent refresh saat visual search
    private var isDisplayProductsLocked = false

    // 🆕 Image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->
                performVisualSearch(imageUri)
            }
        }
    }

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

        // 🆕 Initialize ML Classifier
        farmToolClassifier = FarmToolClassifier(requireContext())

        setupUserRole()
        setupBestSellerRecyclerView() // ✅ NEW: Setup best seller section
        setupRecyclerView()
        loadProducts()
        setupSearch()
        setupFilter()
        setupVisualSearch()
        setupFAB()
    }

    private fun setupUserRole() {
        val sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPreferences.getString("role", "user") ?: "user"
    }

    private fun setupFAB() {
        if (userRole == "seller") {
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
        Log.d("HomeFragment", "🔄 onResume() called, isVisualSearchActive: $isVisualSearchActive")

        // ✅ Skip refresh jika baru selesai visual search
        if (!isVisualSearchActive) {
            loadProductsFromApi()
        } else {
            Log.d("HomeFragment", "⏭️ Skipping refreshProducts() - visual search active")
        }
    }

    // ✅ NEW: Setup Best Seller RecyclerView (Horizontal)
    private fun setupBestSellerRecyclerView() {
        bestSellerAdapter = ProductAdapter(bestSellerProducts, userRole) { product, action ->
            handleProductAction(product, action)
        }

        binding.rvBestSeller.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bestSellerAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(displayProducts, userRole) { product, action ->
            handleProductAction(product, action)
        }

        binding.rvHomeProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
            clipToPadding = false
            isNestedScrollingEnabled = false
        }
    }

    // ✅ NEW: Handle product actions (edit, delete, view, toggle_best_seller)
    private fun handleProductAction(product: Product, action: String) {
        when (action) {
            "edit" -> {
                val bundle = Bundle().apply { putParcelable("product", product) }
                val fragment = EditProductFragment().apply { arguments = bundle }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            "delete" -> {
                deleteProduct(product)
            }
            "view" -> {
                val bundle = Bundle().apply { putParcelable("product", product) }
                val fragment = ProductDetailFragment().apply { arguments = bundle }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            "toggle_best_seller" -> {
                toggleBestSeller(product)
            }
        }
    }

    // ✅ NEW: Toggle Best Seller Status
    private fun toggleBestSeller(product: Product) {
        val newStatus = if (product.isBestSeller == 1) 0 else 1
        val statusText = if (newStatus == 1) "terlaris" else "biasa"

        val sharedPref = requireContext()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Update via API
                val response = ApiClient.apiService.toggleBestSeller(
                    token = "Bearer $token",
                    productId = product.id,
                    isBestSeller = newStatus
                )


                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "✅ Produk ditandai sebagai $statusText",
                        Toast.LENGTH_SHORT
                    ).show()

                    // ✅ Auto refresh untuk update tampilan
                    loadProductsFromApi()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "❌ Gagal update status: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("HomeFragment", "Error toggle best seller: ${e.message}")
            }
        }
    }

    private fun deleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus ${product.name}?")
            .setPositiveButton("Hapus") { dialog, _ ->

                val sharedPref = requireContext()
                    .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                val token = sharedPref.getString("token", "") ?: ""

                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val response = ApiClient.apiService.deleteProduct(
                            token = "Bearer $token",
                            productId = product.id
                        )

                        if (response.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "✅ Produk berhasil dihapus",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadProductsFromApi()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "❌ Gagal hapus: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "❌ Error: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadProducts() {
        loadProductsFromApi()
    }

    // ✅ UPDATED: Load products + separate best sellers
    private fun loadProductsFromApi() {
        if (isDisplayProductsLocked) {
            Log.d("HomeFragment", "🔒 displayProducts LOCKED - skipping API load")
            return
        }

        val sharedPref = requireContext()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getProducts(
                    page = 1,
                    limit = 50,
                    categoryId = null,
                    search = null
                )

                if (response.isSuccessful) {
                    val list = response.body()?.data?.products ?: emptyList()

                    // ✅ Separate best sellers
                    val bestSellers = list.filter { it.isBestSeller == 1 }
                    val allProducts = list

                    // Update Best Seller section
                    bestSellerProducts.clear()
                    bestSellerProducts.addAll(bestSellers)
                    bestSellerAdapter.notifyDataSetChanged()

                    // Update All Products section
                    displayProducts.clear()
                    displayProducts.addAll(allProducts)
                    productAdapter.notifyDataSetChanged()

                    // ✅ Show/Hide Best Seller section
                    if (bestSellers.isEmpty()) {
                        binding.layoutBestSeller.visibility = View.GONE
                    } else {
                        binding.layoutBestSeller.visibility = View.VISIBLE
                    }

                    Log.d("HomeFragment", "✅ Loaded ${list.size} products (${bestSellers.size} best sellers)")
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
                Log.e("HomeFragment", "Error loading products: ${e.message}")
            }
        }
    }

    private fun setupSearch() {
        binding.etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isDisplayProductsLocked) {
                    Log.d("HomeFragment", "🔒 displayProducts LOCKED - skipping search")
                    return
                }

                val query = s.toString().lowercase().trim()

                val filtered = if (query.isEmpty()) {
                    displayProducts
                } else {
                    displayProducts.filter {
                        it.name.lowercase().contains(query) ||
                                it.categoryName?.lowercase()?.contains(query) == true ||
                                it.description?.lowercase()?.contains(query) == true
                    }
                }

                productAdapter.updateList(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupVisualSearch() {
        binding.btnVisualSearch.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("📸 Ambil Foto", "🖼️ Pilih dari Galeri")

        AlertDialog.Builder(requireContext())
            .setTitle("Cari Produk dengan Gambar")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun openCamera() {
        Toast.makeText(requireContext(), "📸 Fitur kamera akan segera hadir!", Toast.LENGTH_SHORT).show()
        openGallery()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun performVisualSearch(imageUri: Uri) {
        try {
            Log.d("HomeFragment", "🔍 Starting visual search...")
            Toast.makeText(requireContext(), "🤖 Menganalisis gambar...", Toast.LENGTH_SHORT).show()

            val predictions = farmToolClassifier.classifyImage(imageUri)

            Log.d("HomeFragment", "✅ ML Predictions: $predictions")

            if (predictions.isEmpty()) {
                Log.d("HomeFragment", "❌ No predictions returned from ML")
                Toast.makeText(
                    requireContext(),
                    "❌ Tidak dapat mengenali produk dalam gambar",
                    Toast.LENGTH_SHORT
                ).show()
                isVisualSearchActive = false
                isDisplayProductsLocked = false
                return
            }

            Log.d("HomeFragment", "📦 Total products in list: ${displayProducts.size}")

            val matchedProducts = displayProducts.filter { product ->
                val productName = product.name.lowercase()
                val categoryName = product.categoryName?.lowercase() ?: ""
                val description = product.description?.lowercase() ?: ""

                val matched = predictions.any { prediction ->
                    productName.contains(prediction) ||
                            categoryName.contains(prediction) ||
                            description.contains(prediction)
                }

                if (matched) {
                    Log.d("HomeFragment", "✅ Matched: ${product.name} (prediction: ${predictions.joinToString()})")
                }

                matched
            }

            Log.d("HomeFragment", "🎯 Total matched products: ${matchedProducts.size}")

            if (matchedProducts.isNotEmpty()) {
                Log.d("HomeFragment", "✅ Updating UI with ${matchedProducts.size} products")

                isDisplayProductsLocked = true
                isVisualSearchActive = true

                displayProducts.clear()
                displayProducts.addAll(matchedProducts)

                Log.d("HomeFragment", "📋 displayProducts updated: ${displayProducts.map { it.name }}")
                Log.d("HomeFragment", "📋 displayProducts size NOW: ${displayProducts.size}")

                productAdapter.notifyDataSetChanged()

                Log.d("HomeFragment", "✅ notifyDataSetChanged() called, adapter size: ${productAdapter.itemCount}")

                binding.rvHomeProducts.post {
                    Log.d("HomeFragment", "🔄 Post-scroll started...")
                    Log.d("HomeFragment", "📊 displayProducts size in post: ${displayProducts.size}")
                    Log.d("HomeFragment", "📊 Adapter itemCount in post: ${productAdapter.itemCount}")

                    binding.rvHomeProducts.scrollToPosition(0)

                    binding.rvHomeProducts.postDelayed({
                        isVisualSearchActive = false
                        isDisplayProductsLocked = false
                        Log.d("HomeFragment", "🚩 Visual search flags reset (unlocked)")
                    }, 2000)
                }

                Toast.makeText(
                    requireContext(),
                    "✅ Ditemukan ${matchedProducts.size} produk serupa!",
                    Toast.LENGTH_LONG
                ).show()

                binding.etSearchHome.setText("")

            } else {
                Log.d("HomeFragment", "❌ No products matched the predictions")
                isVisualSearchActive = false
                isDisplayProductsLocked = false
                Toast.makeText(
                    requireContext(),
                    "❌ Produk tidak ditemukan. Coba gambar lain!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {
            Log.e("HomeFragment", "❌ Error in visual search: ${e.message}")
            e.printStackTrace()
            isVisualSearchActive = false
            isDisplayProductsLocked = false
            Toast.makeText(
                requireContext(),
                "❌ Gagal memproses gambar: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupFilter() {
        binding.btnFilterHome.setOnClickListener {
            val sliderView = layoutInflater.inflate(R.layout.dialog_price_filter, null)
            val slider = sliderView.findViewById<RangeSlider>(R.id.sliderPrice)

            val minPrice = displayProducts.minOfOrNull { it.price } ?: 0.0
            val maxPrice = displayProducts.maxOfOrNull { it.price } ?: 10000000.0

            slider.valueFrom = minPrice.toFloat()
            slider.valueTo = maxPrice.toFloat()
            slider.setValues(minPrice.toFloat(), maxPrice.toFloat())

            AlertDialog.Builder(requireContext())
                .setTitle("Filter Harga")
                .setView(sliderView)
                .setPositiveButton("Terapkan") { dialog, _ ->
                    val values = slider.values
                    val minVal = values[0].toInt()
                    val maxVal = values[1].toInt()

                    val filtered = displayProducts.filter {
                        it.price.toInt() in minVal..maxVal
                    }

                    productAdapter.updateList(filtered)
                    dialog.dismiss()
                }
                .setNegativeButton("Tampilkan Semua") { dialog, _ ->
                    loadProductsFromApi()
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}