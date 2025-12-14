package com.example.miniproject.fragment

import android.content.Context
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
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.adapter.PromoUrlAdapter
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentProductsBinding
import com.example.miniproject.model.Product
import com.example.miniproject.data.model.PromoApi
import kotlinx.coroutines.launch
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

    // === PROMO (ADD/DELETE) ===
    private val promoList = mutableListOf<PromoApi>()
    private lateinit var promoAdapter: PromoUrlAdapter
    private var selectedPromoUri: Uri? = null

    // promo yang sedang tampil di ViewPager (buat delete paling gampang)
    private var currentPromoIndex: Int = 0

    private val promoImagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedPromoUri = it
                uploadPromo(it)
            }
        }
    // === END PROMO ===

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
        loadStoreInfo()

        // === PROMO (ADD/DELETE) ===
        setupPromoCarousel()
        loadPromos()
        // === END PROMO ===
    }

    override fun onResume() {
        super.onResume()
        loadBestSellerProducts()
        loadStoreInfo()

        // === PROMO (ADD/DELETE) ===
        loadPromos()
        // === END PROMO ===
    }

    // ================== USER DATA / ROLE ==================

    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "buyer") ?: "buyer"

        binding.fabAddProduct.visibility = View.GONE

        val isSellerOrAdmin =
            userRole.equals("seller", true) || userRole.equals("admin", true)

        // tombol promo hanya buat seller/admin
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

    private fun setupCategories() {
        CategoryRepository.getCategories()
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
                    val logoPath = settings["app_logo"]?.toString()

                    if (!logoPath.isNullOrEmpty()) {
                        val logoUrl = ApiClient.getImageUrl(logoPath)
                        Glide.with(this@ProductsFragment)
                            .load(logoUrl)
                            .into(binding.imgStoreLogo)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // ================== PROMO (ADD/DELETE) ==================

    private fun setupPromoCarousel() {
        promoAdapter = PromoUrlAdapter(promoList)
        binding.viewPagerPromo.adapter = promoAdapter
        binding.dotsIndicator.attachTo(binding.viewPagerPromo)

        // simpan index yang sedang tampil
        binding.viewPagerPromo.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    currentPromoIndex = position
                }
            }
        )

        // Add promo
        binding.btnAddPromo.setOnClickListener {
            promoImagePicker.launch("image/*")
        }

        // Delete promo (hapus promo yang sedang tampil)
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

                    // reset index biar aman
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
