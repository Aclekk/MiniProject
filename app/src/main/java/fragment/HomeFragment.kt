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
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        setupBestSellerRecyclerView()
        setupRecyclerView()
        loadProducts()
        setupSearch()
        setupFilter()
        setupVisualSearch()
        setupFAB()

        // 🎨 START ANIMATIONS
        startInitialAnimations()
    }

    // 🎨 NEW: Start initial animations when fragment loads
    private fun startInitialAnimations() {
        // Hide all views first
        binding.root.alpha = 0f

        // Fade in entire view
        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    // 🎨 NEW: Animate RecyclerView items with stagger effect
    private fun animateRecyclerView(recyclerView: RecyclerView, startDelay: Long = 0) {
        recyclerView.post {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                child.alpha = 0f
                child.translationY = 50f

                child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(startDelay + (i * 50L))
                    .setDuration(400)
                    .start()
            }
        }
    }

    // 🎨 NEW: Animate FAB with bounce
    private fun animateFAB() {
        binding.fabAddProduct.scaleX = 0f
        binding.fabAddProduct.scaleY = 0f

        binding.fabAddProduct.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(600)
            .setDuration(500)
            .setInterpolator(android.view.animation.BounceInterpolator())
            .start()
    }

    private fun setupUserRole() {
        val sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPreferences.getString("role", "user") ?: "user"
    }

    private fun setupFAB() {
        if (userRole == "seller") {
            binding.fabAddProduct.visibility = View.VISIBLE
            binding.fabAddProduct.setOnClickListener {
                // 🎨 Scale animation on click
                it.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()

                val fragment = AddProductFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            // 🎨 Animate FAB entrance
            animateFAB()
        } else {
            binding.fabAddProduct.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeFragment", "🔄 onResume() called, isVisualSearchActive: $isVisualSearchActive")

        if (!isVisualSearchActive) {
            loadProductsFromApi()
        } else {
            Log.d("HomeFragment", "⏭️ Skipping refreshProducts() - visual search active")
        }
    }

    private fun setupBestSellerRecyclerView() {
        bestSellerAdapter = ProductAdapter(bestSellerProducts, userRole) { product, action ->
            handleProductAction(product, action)
        }

        binding.rvBestSeller.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bestSellerAdapter
            setHasFixedSize(true)

            // 🎨 Add scroll listener for item animations
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Animate newly visible items
                    for (i in 0 until childCount) {
                        val child = getChildAt(i)
                        if (child != null && child.alpha < 1f) {
                            child.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(300)
                                .start()
                        }
                    }
                }
            })
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

            // 🎨 Add scroll listener for item animations
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Animate newly visible items
                    for (i in 0 until childCount) {
                        val child = getChildAt(i)
                        if (child != null && child.alpha < 1f) {
                            child.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(300)
                                .start()
                        }
                    }
                }
            })
        }
    }

    private fun handleProductAction(product: Product, action: String) {
        when (action) {
            "edit" -> {
                val bundle = Bundle().apply { putParcelable("product", product) }
                val fragment = EditProductFragment().apply { arguments = bundle }
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
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
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            "toggle_best_seller" -> {
                toggleBestSeller(product)
            }
        }
    }

    private fun toggleBestSeller(product: Product) {
        val newStatus = if (product.isBestSeller == 1) 0 else 1
        val statusText = if (newStatus == 1) "terlaris" else "biasa"

        val sharedPref = requireContext()
            .getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
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

                    val bestSellers = list.filter { it.isBestSeller == 1 }
                    val allProducts = list

                    // Update Best Seller section
                    bestSellerProducts.clear()
                    bestSellerProducts.addAll(bestSellers)
                    bestSellerAdapter.notifyDataSetChanged()

                    // 🎨 Animate best seller items
                    if (bestSellers.isNotEmpty()) {
                        animateRecyclerView(binding.rvBestSeller, 200)
                    }

                    // Update All Products section
                    displayProducts.clear()
                    displayProducts.addAll(allProducts)
                    productAdapter.notifyDataSetChanged()

                    // 🎨 Animate product grid items
                    animateRecyclerView(binding.rvHomeProducts, 400)

                    // Show/Hide Best Seller section
                    if (bestSellers.isEmpty()) {
                        binding.layoutBestSeller.visibility = View.GONE
                    } else {
                        binding.layoutBestSeller.visibility = View.VISIBLE
                        // 🎨 Animate section appearance
                        binding.layoutBestSeller.alpha = 0f
                        binding.layoutBestSeller.animate()
                            .alpha(1f)
                            .setDuration(500)
                            .setStartDelay(100)
                            .start()
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

                // 🎨 Animate filtered results
                binding.rvHomeProducts.postDelayed({
                    animateRecyclerView(binding.rvHomeProducts, 0)
                }, 100)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupVisualSearch() {
        binding.btnVisualSearch.setOnClickListener {
            // 🎨 Button press animation
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()

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

                    // 🎨 Animate search results
                    animateRecyclerView(binding.rvHomeProducts, 0)

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
            // 🎨 Button press animation
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()

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

                    // 🎨 Animate filtered results
                    binding.rvHomeProducts.postDelayed({
                        animateRecyclerView(binding.rvHomeProducts, 0)
                    }, 100)

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