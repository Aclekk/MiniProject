package com.example.miniproject.fragment

import androidx.appcompat.app.AlertDialog
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.repository.ProductRepository
import com.example.miniproject.databinding.FragmentHomeBinding
import com.example.miniproject.ml.WaterQualityClassifier
import com.example.miniproject.model.Product
import com.example.miniproject.viewmodel.ProductsViewModel
import com.example.miniproject.viewmodel.ProductsViewModelFactory
import com.google.android.material.slider.RangeSlider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var bestSellerAdapter: ProductAdapter

    private val displayProducts = mutableListOf<Product>()
    private val bestSellerProducts = mutableListOf<Product>()
    private val allProductsBackup = mutableListOf<Product>() // ✅ Backup untuk reset

    private var userRole = "user"

    // ✅ Water Quality Classifier
    private lateinit var waterQualityClassifier: WaterQualityClassifier

    // Flags untuk prevent refresh
    private var isWaterCheckActive = false
    private var isDisplayProductsLocked = false

    // ✅ MVVM: ViewModel
    private lateinit var viewModel: ProductsViewModel

    // Image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->
                performWaterQualityCheck(imageUri)
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

        // ✅ Initialize Water Quality ML Classifier
        waterQualityClassifier = WaterQualityClassifier(requireContext())

        setupUserRole()
        setupViewModel() // ✅ MVVM init
        setupBestSellerRecyclerView()
        setupRecyclerView()
        setupObservers() // ✅ observe data dari VM

        // ✅ Trigger load pertama kali
        loadProducts()

        setupSearch()
        setupFilter()
        setupWaterQualityButton()
        setupFAB()
    }

    private fun setupUserRole() {
        val sharedPreferences =
            requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPreferences.getString("role", "user") ?: "user"
    }

    // ✅ MVVM init
    private fun setupViewModel() {
        val repository = ProductRepository(ApiClient.apiService)
        val factory = ProductsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProductsViewModel::class.java]
    }

    // ✅ Observe LiveData dari ViewModel
    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { list ->
            // Kalau sedang water check & lock, jangan override list user
            if (isDisplayProductsLocked) {
                Log.d("HomeFragment", "🔒 LOCKED - skip update from ViewModel")
                return@observe
            }

            val bestSellers = list.filter { it.isBestSeller == 1 }

            // Update backup
            allProductsBackup.clear()
            allProductsBackup.addAll(list)

            // Update Best Seller section
            bestSellerProducts.clear()
            bestSellerProducts.addAll(bestSellers)
            bestSellerAdapter.notifyDataSetChanged()

            // Update All Products section
            displayProducts.clear()
            displayProducts.addAll(list)
            productAdapter.notifyDataSetChanged()

            // Show/Hide Best Seller section
            binding.layoutBestSeller.visibility =
                if (bestSellers.isEmpty()) View.GONE else View.VISIBLE

            Log.d(
                "HomeFragment",
                "✅ VM delivered ${list.size} products (${bestSellers.size} best sellers)"
            )
        }

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
        Log.d("HomeFragment", "🔄 onResume() called, isWaterCheckActive: $isWaterCheckActive")

        if (!isWaterCheckActive) {
            // ✅ MVVM: refresh via VM
            viewModel.loadProducts()
        } else {
            Log.d("HomeFragment", "⏭ Skipping refresh - water check active")
        }
    }

    private fun setupBestSellerRecyclerView() {
        bestSellerAdapter = ProductAdapter(bestSellerProducts, userRole) { product, action ->
            handleProductAction(product, action)
        }

        binding.rvBestSeller.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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

            "delete" -> deleteProduct(product)

            "view" -> {
                val bundle = Bundle().apply { putParcelable("product", product) }
                val fragment = ProductDetailFragment().apply { arguments = bundle }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            "toggle_best_seller" -> toggleBestSeller(product)
        }
    }

    // ✅ MVVM: toggle best seller via ViewModel
    private fun toggleBestSeller(product: Product) {
        val sharedPref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", "") ?: ""

        viewModel.toggleBestSeller(
            token = "Bearer $token",
            productId = product.id!!,
            currentStatus = product.isBestSeller ?: 0

        )


    }

    // ✅ MVVM: delete via ViewModel
    private fun deleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus ${product.name}?")
            .setPositiveButton("Hapus") { dialog, _ ->
                val sharedPref =
                    requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                val token = sharedPref.getString("token", "") ?: ""

                viewModel.deleteProduct(
                    token = "Bearer $token",
                    productId = product.id!!
                )



                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadProducts() {
        // ✅ MVVM: trigger load from ViewModel
        viewModel.loadProducts()
    }

    // ================== SEARCH ==================
    private fun setupSearch() {
        binding.etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isDisplayProductsLocked) {
                    Log.d("HomeFragment", "🔒 displayProducts LOCKED - skipping search")
                    return
                }

                val query = s.toString().lowercase().trim()

                val filtered = if (query.isEmpty()) {
                    allProductsBackup
                } else {
                    allProductsBackup.filter {
                        it.name.lowercase().contains(query) ||
                                it.categoryName?.lowercase()?.contains(query) == true ||
                                it.description?.lowercase()?.contains(query) == true
                    }
                }

                productAdapter.updateList(filtered)
            }
        })
    }

    // ================== WATER QUALITY ==================
    private fun setupWaterQualityButton() {
        binding.btnVisualSearch.setOnClickListener {
            showWaterCheckDialog()
        }
    }

    private fun showWaterCheckDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🌊 Analisis Kualitas Air")
            .setMessage("Upload foto air (sawah/kolam/irigasi) untuk mendapat rekomendasi produk yang tepat")
            .setIcon(R.drawable.ic_water)
            .setPositiveButton("📸 Ambil Foto") { _, _ ->
                Toast.makeText(context, "📸 Kamera segera hadir!", Toast.LENGTH_SHORT).show()
                openGallery()
            }
            .setNeutralButton("🖼 Galeri") { _, _ ->
                openGallery()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun performWaterQualityCheck(imageUri: Uri) {
        try {
            Log.d("HomeFragment", "🌊 Starting water quality analysis...")
            Toast.makeText(requireContext(), "🤖 Menganalisis kualitas air...", Toast.LENGTH_SHORT).show()

            // Water quality masih di Fragment (boleh dulu)
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val result = waterQualityClassifier.analyzeWater(imageUri)

                    if (result == null) {
                        Toast.makeText(
                            requireContext(),
                            "❌ Gagal menganalisis gambar.\n" +
                                    "Pastikan:\n" +
                                    "1. Flask server running di http://192.168.100.18:5000\n" +
                                    "2. HP & PC di WiFi yang sama\n" +
                                    "3. Firewall allows port 5000",
                            Toast.LENGTH_LONG
                        ).show()
                        isWaterCheckActive = false
                        isDisplayProductsLocked = false
                        return@launch
                    }

                    Log.d("HomeFragment", "✅ ML Result: ${result.quality} (${result.confidence})")
                    Log.d("HomeFragment", "   Probabilities: ${result.probabilities}")

                    if (result.quality == "Tidak Valid" || result.quality == "Tidak Yakin") {
                        Toast.makeText(
                            requireContext(),
                            "⚠️ Foto tidak cukup jelas / bukan air.\nMenampilkan semua produk.",
                            Toast.LENGTH_LONG
                        ).show()

                        isWaterCheckActive = false
                        isDisplayProductsLocked = false

                        displayProducts.clear()
                        displayProducts.addAll(allProductsBackup)
                        productAdapter.notifyDataSetChanged()

                        return@launch
                    }

                    showWaterQualityResult(result)
                    filterProductsByWaterQuality(result.quality)

                } catch (e: Exception) {
                    Log.e("HomeFragment", "❌ Error in coroutine: ${e.message}")
                    e.printStackTrace()
                    isWaterCheckActive = false
                    isDisplayProductsLocked = false
                    Toast.makeText(requireContext(), "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            Log.e("HomeFragment", "❌ Error in water check: ${e.message}")
            e.printStackTrace()
            isWaterCheckActive = false
            isDisplayProductsLocked = false
            Toast.makeText(requireContext(), "❌ Gagal memproses gambar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showWaterQualityResult(result: WaterQualityClassifier.WaterQualityResult) {
        val emoji = when (result.quality) {
            "Jernih" -> "✅"
            "Keruh" -> "⚠️"
            "Tercemar" -> "❌"
            else -> "ℹ️"
        }

        val confidence = (result.confidence * 100).toInt()

        AlertDialog.Builder(requireContext())
            .setTitle("$emoji Hasil Analisis Kualitas Air")
            .setMessage(
                """
                Kualitas: ${result.quality}
                Tingkat Kepercayaan: $confidence%
                
                ${result.recommendation}
                
                Produk yang direkomendasikan akan ditampilkan di bawah.
            """.trimIndent()
            )
            .setPositiveButton("Lihat Produk Rekomendasi") { dialog, _ ->
                dialog.dismiss()
                binding.rvHomeProducts.smoothScrollToPosition(0)
            }
            .setNegativeButton("Tampilkan Semua Produk") { dialog, _ ->
                isDisplayProductsLocked = false
                isWaterCheckActive = false
                displayProducts.clear()
                displayProducts.addAll(allProductsBackup)
                productAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun norm(s: String): String {
        return s.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun filterProductsByWaterQuality(quality: String) {
        isDisplayProductsLocked = true
        isWaterCheckActive = true

        val keywords = waterQualityClassifier.getRecommendedKeywords(quality)
        Log.d("HomeFragment", "🎯 Filtering by keywords: $keywords")

        val matchedProducts = allProductsBackup.filter { product ->
            val productName = norm(product.name)
            val description = norm(product.description ?: "")

            keywords.any { keyword ->
                val k = norm(keyword)
                productName.contains(k) || description.contains(k)
            }
        }

        Log.d("HomeFragment", "🎯 Total matched products: ${matchedProducts.size}")

        if (matchedProducts.isNotEmpty()) {
            displayProducts.clear()
            displayProducts.addAll(matchedProducts)
            productAdapter.notifyDataSetChanged()

            binding.rvHomeProducts.scrollToPosition(0)

            Toast.makeText(
                requireContext(),
                "✅ Menampilkan ${matchedProducts.size} produk yang cocok untuk air ${quality.lowercase()}",
                Toast.LENGTH_LONG
            ).show()

            binding.rvHomeProducts.postDelayed({
                isWaterCheckActive = false
            }, 3000)

        } else {
            Toast.makeText(
                requireContext(),
                "ℹ️ Belum ada produk yang cocok. Menampilkan semua produk.",
                Toast.LENGTH_SHORT
            ).show()
            isWaterCheckActive = false
            isDisplayProductsLocked = false
            displayProducts.clear()
            displayProducts.addAll(allProductsBackup)
            productAdapter.notifyDataSetChanged()
        }
    }

    // ================== FILTER HARGA ==================
    private fun setupFilter() {
        binding.btnFilterHome.setOnClickListener {
            val sliderView = layoutInflater.inflate(R.layout.dialog_price_filter, null)
            val slider = sliderView.findViewById<RangeSlider>(R.id.sliderPrice)

            val sourceList = if (displayProducts.isEmpty()) allProductsBackup else displayProducts

            val minPrice = sourceList.minOfOrNull { it.price } ?: 0.0
            val maxPrice = sourceList.maxOfOrNull { it.price } ?: 10000000.0

            val stepSize = 50000f
            val roundedMin = ((minPrice / stepSize).toInt() * stepSize).toFloat()
            val roundedMax = ((maxPrice / stepSize).toInt() * stepSize + stepSize).toFloat()

            slider.valueFrom = roundedMin
            slider.valueTo = roundedMax
            slider.setValues(roundedMin, roundedMax)

            AlertDialog.Builder(requireContext())
                .setTitle("Filter Harga")
                .setView(sliderView)
                .setPositiveButton("Terapkan") { dialog, _ ->
                    val values = slider.values
                    val minVal = values[0].toInt()
                    val maxVal = values[1].toInt()

                    val filtered = sourceList.filter {
                        it.price.toInt() in minVal..maxVal
                    }

                    productAdapter.updateList(filtered)

                    Toast.makeText(requireContext(), "✅ Ditampilkan ${filtered.size} produk", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Tampilkan Semua") { dialog, _ ->
                    isDisplayProductsLocked = false
                    isWaterCheckActive = false
                    viewModel.loadProducts() // ✅ refresh via VM
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onDestroyView() {
        waterQualityClassifier.close()
        super.onDestroyView()
        _binding = null
    }
}
