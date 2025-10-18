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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.ProductDataSource
import com.example.miniproject.databinding.FragmentHomeBinding
import com.example.miniproject.ml.FarmToolClassifier
import com.example.miniproject.model.Product
import com.google.android.material.slider.RangeSlider

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val displayProducts = mutableListOf<Product>()
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
        setupRecyclerView()
        loadProducts()
        setupSearch()
        setupFilter()
        setupVisualSearch() // 🆕 Setup visual search button
        setupFAB()
    }

    private fun setupUserRole() {
        val sharedPreferences = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPreferences.getString("role", "user") ?: "user"
    }

    private fun setupFAB() {
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
        Log.d("HomeFragment", "🔄 onResume() called, isVisualSearchActive: $isVisualSearchActive")

        // ✅ Skip refresh jika baru selesai visual search
        if (!isVisualSearchActive) {
            refreshProducts()
        } else {
            Log.d("HomeFragment", "⏭️ Skipping refreshProducts() - visual search active")
        }
    }

    private fun setupRecyclerView() {
        // ✅ Pass displayProducts langsung ke adapter (shared reference)
        productAdapter = ProductAdapter(displayProducts, userRole) { product, action ->
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
                    AlertDialog.Builder(requireContext())
                        .setTitle("Hapus Produk")
                        .setMessage("Yakin ingin menghapus ${product.name}?")
                        .setPositiveButton("Hapus") { dialog, _ ->
                            ProductDataSource.deleteProduct(product)
                            refreshProducts()
                            Toast.makeText(context, "✅ Produk dihapus", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
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
        ProductDataSource.loadDummyData()
        refreshProducts()
    }

    private fun refreshProducts() {
        // 🔒 Skip jika displayProducts sedang di-lock
        if (isDisplayProductsLocked) {
            Log.d("HomeFragment", "🔒 displayProducts LOCKED - skipping refresh")
            return
        }

        val allProducts = ProductDataSource.getAllProducts()
        displayProducts.clear()
        displayProducts.addAll(allProducts)

        // ✅ Karena displayProducts adalah shared reference, cukup notify
        productAdapter.notifyDataSetChanged()

        Log.d("HomeFragment", "🔄 refreshProducts() - displayProducts size: ${displayProducts.size}")
    }

    private fun setupSearch() {
        binding.etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 🔒 Skip jika displayProducts sedang di-lock
                if (isDisplayProductsLocked) {
                    Log.d("HomeFragment", "🔒 displayProducts LOCKED - skipping search")
                    return
                }

                val query = s.toString().lowercase().trim()
                val allProducts = ProductDataSource.getAllProducts()
                val filtered = if (query.isEmpty()) {
                    allProducts
                } else {
                    allProducts.filter {
                        it.name.lowercase().contains(query) ||
                                it.categoryName?.lowercase()?.contains(query) == true ||
                                it.description?.lowercase()?.contains(query) == true
                    }
                }

                displayProducts.clear()
                displayProducts.addAll(filtered)

                // ✅ Karena shared reference, cukup notify
                productAdapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // 🆕 Setup Visual Search Button
    private fun setupVisualSearch() {
        binding.btnVisualSearch.setOnClickListener {
            showImageSourceDialog()
        }
    }

    // 🆕 Show dialog: Camera or Gallery
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

    // 🆕 Open Camera
    private fun openCamera() {
        // TODO: Implement camera capture (need CameraX implementation)
        Toast.makeText(requireContext(), "📸 Fitur kamera akan segera hadir!", Toast.LENGTH_SHORT).show()
        // For now, fallback to gallery
        openGallery()
    }

    // 🆕 Open Gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    // 🆕 Perform Visual Search with ML (✅ FINAL FIXED VERSION with LOCK)
    private fun performVisualSearch(imageUri: Uri) {
        try {
            Log.d("HomeFragment", "🔍 Starting visual search...")
            Toast.makeText(requireContext(), "🤖 Menganalisis gambar...", Toast.LENGTH_SHORT).show()

            // 🎯 Classify image using ML
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

            // 🔍 Filter products based on predictions
            val allProducts = ProductDataSource.getAllProducts()
            Log.d("HomeFragment", "📦 Total products in DB: ${allProducts.size}")

            val matchedProducts = allProducts.filter { product ->
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

            // 📊 Update UI (✅ FINAL FIX with LOCK)
            if (matchedProducts.isNotEmpty()) {
                Log.d("HomeFragment", "✅ Updating UI with ${matchedProducts.size} products")

                // 🔒🔒🔒 LOCK displayProducts agar tidak bisa diubah!
                isDisplayProductsLocked = true
                isVisualSearchActive = true

                // ✅ Update displayProducts (shared reference dengan adapter)
                displayProducts.clear()
                displayProducts.addAll(matchedProducts)

                Log.d("HomeFragment", "📋 displayProducts updated: ${displayProducts.map { it.name }}")
                Log.d("HomeFragment", "📋 displayProducts size NOW: ${displayProducts.size}")

                // 🔥 PENTING: Notify LANGSUNG setelah update list!
                productAdapter.notifyDataSetChanged()

                Log.d("HomeFragment", "✅ notifyDataSetChanged() called, adapter size: ${productAdapter.itemCount}")

                // 🔄 Scroll dan reset flag
                binding.rvHomeProducts.post {
                    Log.d("HomeFragment", "🔄 Post-scroll started...")
                    Log.d("HomeFragment", "📊 displayProducts size in post: ${displayProducts.size}")
                    Log.d("HomeFragment", "📊 Adapter itemCount in post: ${productAdapter.itemCount}")

                    binding.rvHomeProducts.scrollToPosition(0)

                    // ✅ Reset SEMUA flag setelah 2 detik
                    binding.rvHomeProducts.postDelayed({
                        isVisualSearchActive = false
                        isDisplayProductsLocked = false  // 🔓 UNLOCK!
                        Log.d("HomeFragment", "🚩 Visual search flags reset (unlocked)")
                    }, 2000)
                }

                Toast.makeText(
                    requireContext(),
                    "✅ Ditemukan ${matchedProducts.size} produk serupa!",
                    Toast.LENGTH_LONG
                ).show()

                // ✅ Clear search text
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

            val allProducts = ProductDataSource.getAllProducts()
            val minPrice = allProducts.minOfOrNull { it.price } ?: 0.0
            val maxPrice = allProducts.maxOfOrNull { it.price } ?: 10000000.0

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

                    val filtered = allProducts.filter {
                        it.price.toInt() in minVal..maxVal
                    }

                    displayProducts.clear()
                    displayProducts.addAll(filtered)

                    // ✅ Changed from updateList() - karena shared reference
                    productAdapter.notifyDataSetChanged()
                    dialog.dismiss()
                }
                .setNegativeButton("Tampilkan Semua") { dialog, _ ->
                    displayProducts.clear()
                    displayProducts.addAll(allProducts)

                    // ✅ Changed from updateList() - karena shared reference
                    productAdapter.notifyDataSetChanged()
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