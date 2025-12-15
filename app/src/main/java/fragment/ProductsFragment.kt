package com.example.miniproject.fragment

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.adapter.PromoUrlAdapter
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentProductsBinding
import com.example.miniproject.model.Category
import com.example.miniproject.model.Product
import com.example.miniproject.data.model.PromoApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()
    private var userRole: String = "buyer"

    // === CATEGORY ADAPTER ===
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    // === PROMO (ADD/DELETE) ===
    private val promoList = mutableListOf<PromoApi>()
    private lateinit var promoAdapter: PromoUrlAdapter
    private var selectedPromoUri: Uri? = null
    private var currentPromoIndex: Int = 0

    private val promoImagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedPromoUri = it
                uploadPromo(it)
            }
        }

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
        setupCategoriesHorizontal() // ✅ Setup horizontal categories
        setupClickListeners()
        setupSearch()
        loadBestSellerProducts()
        loadCategories() // ✅ Load categories data
        loadStoreInfo()
        setupPromoCarousel()
        loadPromos()
    }

    override fun onResume() {
        super.onResume()
        loadBestSellerProducts()
        loadCategories() // ✅ Reload categories
        loadStoreInfo()
        loadPromos()
    }

    // ================== USER DATA / ROLE ==================

    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "buyer") ?: "buyer"

        binding.fabAddProduct.visibility = View.GONE

        val isSellerOrAdmin =
            userRole.equals("seller", true) || userRole.equals("admin", true)

        binding.btnAddPromo.visibility = if (isSellerOrAdmin) View.VISIBLE else View.GONE
        binding.btnDeletePromo.visibility = if (isSellerOrAdmin) View.VISIBLE else View.GONE
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

    // ================== HORIZONTAL CATEGORIES ==================
    // ✅ Setup horizontal scroll untuk kategori

    private fun setupCategoriesHorizontal() {
        // 1. Setup adapter untuk kategori
        categoryAdapter = CategoryAdapter(
            categories = categories,
            userRole = userRole,
            onItemClick = { category ->
                // Navigate ke CategoriesFragment dengan filter
                val bundle = Bundle().apply {
                    putInt("category_id", category.id)
                    putString("category_name", category.categoryName)
                }
                val fragment = CategoriesFragment().apply {
                    arguments = bundle
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onEditClick = null, // Tidak perlu di Home
            onDeleteClick = null // Tidak perlu di Home
        )

        // 2. Setup Linear Layout Manager (HORIZONTAL)
        val layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvCategories.layoutManager = layoutManager

        // 3. ✨ SMOOTH SNAP EFFECT (optional - bikin snap ke tengah saat scroll)
        val snapHelper = LinearSnapHelper()
        try {
            snapHelper.attachToRecyclerView(binding.rvCategories)
        } catch (e: IllegalStateException) {
            // Already attached, ignore
        }

        // 4. ✨ SPACING ANTAR ITEM (biar ga tempel)
        val spacingInPixels = 16 // 16dp spacing
        binding.rvCategories.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)

                // Spacing kanan untuk semua item
                outRect.right = spacingInPixels

                // Spacing kiri untuk item pertama (optional)
                if (position == 0) {
                    outRect.left = spacingInPixels
                }
            }
        })

        // 5. ✨ DISABLE OVERSCROLL GLOW (biar smooth)
        binding.rvCategories.overScrollMode = View.OVER_SCROLL_NEVER

        // 6. ✅ SET ADAPTER ke RecyclerView
        binding.rvCategories.adapter = categoryAdapter
    }

    // ================== LOAD CATEGORIES ==================
    // ✅ Load categories dari repository

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Load categories di background thread
                val categoryList = withContext(Dispatchers.IO) {
                    CategoryRepository.getCategories()
                }

                // Update UI di main thread
                categories.clear()
                categories.addAll(categoryList)
                categoryAdapter.notifyDataSetChanged()

                android.util.Log.d("ProductsFragment", "✅ Categories loaded: ${categories.size} items")
                categories.forEach {
                    android.util.Log.d("ProductsFragment", "   - ${it.categoryName}")
                }

                if (categories.isEmpty()) {
                    Toast.makeText(requireContext(), "Belum ada kategori", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                android.util.Log.e("ProductsFragment", "❌ Error loading categories: ${e.message}")
                Toast.makeText(
                    requireContext(),
                    "Gagal load kategori: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ================== CLICK LISTENERS ==================

    private fun setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadBestSellerProducts()
            loadCategories() // ✅ Reload categories saat refresh
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

    // ================== LOAD STORE INFO ==================

    private fun loadStoreInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getSettings()
                if (response.isSuccessful && response.body()?.success == true) {
                    val settings = response.body()?.data as? Map<String, Any> ?: emptyMap()

                    val appName = settings["app_name"]?.toString() ?: "namek gacor wjwje"
                    val appTagline = settings["app_tagline"]?.toString() ?: "rachen kapten"
                    val appLogo = settings["app_logo"]?.toString() ?: ""
                    val appAddress = settings["app_address"]?.toString() ?: "ah ah h"
                    val contactEmail = settings["contact_email"]?.toString() ?: "support@agrishop.com"
                    val contactPhone = settings["contact_phone"]?.toString() ?: "081234567890"

                    binding.tvStoreName.text = "🌾 $appName"
                    binding.tvStoreTagline.text = appTagline

                    if (appLogo.isNotEmpty()) {
                        val logoUrl = ApiClient.getImageUrl(appLogo)
                        Glide.with(this@ProductsFragment)
                            .load(logoUrl)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(binding.imgStoreLogo)
                    } else {
                        binding.imgStoreLogo.setImageResource(R.drawable.ic_person)
                    }

                    binding.tvAboutTagline.text = appTagline
                    binding.tvAboutAddress.text = "📍 $appAddress"
                    binding.tvAboutPhone.text = "📞 $contactPhone"
                    binding.tvAboutEmail.text = "📧 $contactEmail"
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductsFragment", "❌ Error loading store info: ${e.message}")
            }
        }
    }

    // ================== PROMO (ADD/DELETE) ==================

    private fun setupPromoCarousel() {
        promoAdapter = PromoUrlAdapter(promoList)
        binding.viewPagerPromo.adapter = promoAdapter
        binding.dotsIndicator.attachTo(binding.viewPagerPromo)

        binding.viewPagerPromo.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentPromoIndex = position
                }
            }
        )

        binding.btnAddPromo.setOnClickListener {
            promoImagePicker.launch("image/*")
        }

        binding.btnDeletePromo.setOnClickListener {
            if (promoList.isEmpty()) {
                Toast.makeText(requireContext(), "Belum ada promo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idx = currentPromoIndex.coerceIn(0, promoList.lastIndex)
            val promoId = promoList[idx].id

            deletePromo(promoId)
        }
    }

    private fun loadPromos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.apiService.getActivePromos()
                if (res.isSuccessful && res.body()?.success == true) {
                    promoList.clear()
                    promoList.addAll(res.body()?.data?.promos ?: emptyList())
                    promoAdapter.notifyDataSetChanged()
                    currentPromoIndex = 0
                }
            } catch (_: Exception) {}
        }
    }

    private fun uploadPromo(uri: Uri) {
        val pref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = pref.getString("token", null) ?: run {
            Toast.makeText(requireContext(), "Token kosong. Login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val input = requireContext().contentResolver.openInputStream(uri)
                    ?: throw Exception("Gagal baca file")

                val file = File.createTempFile("promo_", ".jpg", requireContext().cacheDir)
                file.outputStream().use { out -> input.copyTo(out) }

                val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", file.name, body)

                val res = ApiClient.apiService.uploadPromo("Bearer $token", part)
                if (res.isSuccessful && res.body()?.success == true) {
                    Toast.makeText(requireContext(), "Promo berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    loadPromos()
                } else {
                    val err = res.errorBody()?.string()
                    Toast.makeText(requireContext(), "Gagal tambah promo: ${res.code()} $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error upload", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deletePromo(promoId: Int) {
        val pref = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = pref.getString("token", null) ?: run {
            Toast.makeText(requireContext(), "Token kosong. Login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = ApiClient.apiService.deletePromo(
                    authHeader = "Bearer $token",
                    body = mapOf("id" to promoId)
                )

                if (res.isSuccessful && res.body()?.success == true) {
                    Toast.makeText(requireContext(), "Promo berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadPromos()
                } else {
                    val err = res.errorBody()?.string()
                    Toast.makeText(requireContext(), "Gagal hapus promo: ${res.code()} $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error delete", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ================== CLEANUP ==================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}