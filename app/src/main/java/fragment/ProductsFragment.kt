package com.example.miniproject.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.adapter.PromoAdapter
import com.example.miniproject.databinding.FragmentProductsBinding
import com.example.miniproject.model.Category
import com.example.miniproject.model.Product
import com.example.miniproject.model.Promo
import com.example.miniproject.utils.DummyData
import com.example.miniproject.utils.ProductStorage

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var promoAdapter: PromoAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()
    private var userRole = ""
    private lateinit var storePrefListener: SharedPreferences.OnSharedPreferenceChangeListener

    companion object {
        private const val TAG = "ProductsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getUserData()
        loadStoreProfile()
        loadAboutUs()
        setupCarousel()
        setupCategories()
        setupRecyclerView()
        setupClickListeners()
        setupSearchAndFilter()
        loadProductsFromStorage()
    }

    // ============================================================
    // 🏪 STORE PROFILE (Nama & Foto Toko Real-time)
    // ============================================================
    private fun loadStoreProfile() {
        val prefs = requireContext().getSharedPreferences("store_pref", Context.MODE_PRIVATE)
        updateStoreUI(prefs)

        storePrefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                "store_name", "photo_uri" -> updateStoreUI(sharedPrefs)
                "store_about", "store_address", "store_contact" -> updateAboutUsUI(sharedPrefs)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(storePrefListener)
    }

    private fun updateStoreUI(prefs: SharedPreferences) {
        val storeName = prefs.getString("store_name", "Toko Niaga Tani")
        val photoUri = prefs.getString("photo_uri", null)

        binding.tvStoreName.text = storeName ?: "Toko Niaga Tani"

        if (photoUri != null) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(Uri.parse(photoUri))
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.ivStoreProfile.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.ivStoreProfile.setImageResource(R.drawable.ic_store_default)
            }
        } else {
            binding.ivStoreProfile.setImageResource(R.drawable.ic_store_default)
        }
    }

    // ============================================================
    // 📖 Tentang Kami
    // ============================================================
    private fun loadAboutUs() {
        val prefs = requireContext().getSharedPreferences("store_pref", Context.MODE_PRIVATE)
        updateAboutUsUI(prefs)
    }

    private fun updateAboutUsUI(prefs: SharedPreferences) {
        val storeName = prefs.getString("store_name", "Toko Niaga Tani")
        val aboutUsText = prefs.getString(
            "store_about",
            "$storeName berkomitmen menyediakan alat dan kebutuhan pertanian terbaik untuk petani Indonesia 🌾"
        )
        val storeAddress = prefs.getString("store_address", "Jl. Pertanian No. 1, Tangerang")
        val storeContact = prefs.getString("store_contact", "08123456789")

        binding.tvAboutUsTitle.text = "Tentang Kami"
        binding.tvAboutUsContent.text = aboutUsText
        binding.tvStoreAddress.text = storeAddress
        binding.tvStoreContact.text = "Telp: $storeContact"
    }

    // ============================================================
    // 👤 ROLE
    // ============================================================
    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "user") ?: "user"
        binding.fabAddProduct.visibility =
            if (userRole == "admin") View.VISIBLE else View.GONE
    }

    // ============================================================
    // 🎠 PROMO
    // ============================================================
    private fun setupCarousel() {
        val promos = listOf(
            Promo(1, "Diskon 30% Pupuk Organik", "Dapatkan pupuk organik berkualitas", R.drawable.promo1),
            Promo(2, "Beli Cangkul Gratis Sekop", "Penawaran terbatas!", R.drawable.promo2),
            Promo(3, "Flash Sale Benih Padi", "Harga spesial hari ini!", R.drawable.promo3)
        )
        promoAdapter = PromoAdapter(promos)
        binding.viewPagerPromo.adapter = promoAdapter
        binding.dotsIndicator.attachTo(binding.viewPagerPromo)
    }

    // ============================================================
    // 📂 KATEGORI
    // ============================================================
    private fun setupCategories() {
        val categories = listOf(
            Category(1, "Peralatan", "2025-01-01"),
            Category(2, "Pupuk", "2025-01-01"),
            Category(3, "Benih", "2025-01-01"),
            Category(4, "Alat Pertanian", "2025-01-01")
        )
        categoryAdapter = CategoryAdapter(categories) { selected ->
            filterByCategory(selected.categoryName)
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    // ============================================================
    // 🛒 PRODUK GRID
    // ============================================================
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "edit" -> editProduct(product)
                "delete" -> deleteProduct(product)
                "view" -> viewProduct(product)
                "refresh" -> loadProductsFromStorage()
            }
        }

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            adapter = productAdapter
            setHasFixedSize(true)
            clipToPadding = false
            isNestedScrollingEnabled = false
        }
    }

    // ============================================================
    // 🔹 HANDLER
    // ============================================================
    private fun setupClickListeners() {
        binding.fabAddProduct.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddProductFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadProductsFromStorage()
        }
    }

    // ============================================================
    // 📦 LOAD PRODUK
    // ============================================================
    private fun loadProductsFromStorage() {
        showLoading(true)
        Log.d(TAG, "🔄 Memulai loadProductsFromStorage()")

        val context = requireContext()
        val storedProducts = ProductStorage.loadProducts(context)

        Log.d(TAG, "📊 Jumlah produk tersimpan: ${storedProducts.size}")

        if (storedProducts.isEmpty()) {
            Log.d(TAG, "⚠️ Data kosong! Loading dummy products...")
            loadDummyProducts()
        } else {
            Log.d(TAG, "✅ Menggunakan data tersimpan")
            allProducts.clear()
            allProducts.addAll(storedProducts)
            products.clear()
            products.addAll(storedProducts)
            productAdapter.notifyDataSetChanged()
        }

        showLoading(false)
        binding.swipeRefresh.isRefreshing = false

        Log.d(TAG, "🎉 Berhasil load ${products.size} produk")
    }

    private fun loadDummyProducts() {
        // ✅ Pakai central dummy data yang sama dengan HomeFragment
        val dummyProducts = DummyData.getDummyProducts()

        ProductStorage.saveProducts(requireContext(), dummyProducts)
        Log.d(TAG, "✅ ${dummyProducts.size} dummy products berhasil disimpan!")

        allProducts.clear()
        allProducts.addAll(dummyProducts)
        products.clear()
        products.addAll(dummyProducts)
        productAdapter.notifyDataSetChanged()
    }

    // ============================================================
    // 🔍 SEARCH & FILTER
    // ============================================================
    private fun setupSearchAndFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString()?.lowercase() ?: ""
                val filtered = allProducts.filter {
                    it.name.lowercase().contains(text) ||
                            it.categoryName?.lowercase()?.contains(text) == true
                }
                products.clear()
                products.addAll(filtered)
                productAdapter.notifyDataSetChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnFilter.setOnClickListener {
            val categories = allProducts.mapNotNull { it.categoryName }.distinct()
            AlertDialog.Builder(requireContext())
                .setTitle("Filter Produk")
                .setItems(categories.toTypedArray()) { _, which ->
                    filterByCategory(categories[which])
                }
                .setNegativeButton("Tampilkan Semua") { _, _ ->
                    products.clear()
                    products.addAll(allProducts)
                    productAdapter.notifyDataSetChanged()
                }
                .show()
        }
    }

    // ============================================================
    // ⚙️ UTIL
    // ============================================================
    private fun filterByCategory(category: String) {
        val filtered = allProducts.filter { it.categoryName == category }
        products.clear()
        products.addAll(filtered)
        productAdapter.notifyDataSetChanged()
    }

    private fun editProduct(product: Product) {
        val fragment = AddProductFragment().apply {
            arguments = Bundle().apply {
                putParcelable("product_data", product)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteProduct(product: Product) {
        val context = requireContext()
        val list = ProductStorage.loadProducts(context).toMutableList()
        list.removeAll { it.id == product.id }
        ProductStorage.saveProducts(context, list)
        loadProductsFromStorage()
        Toast.makeText(context, "Produk dihapus ✅", Toast.LENGTH_SHORT).show()
    }

    private fun viewProduct(product: Product) {
        Toast.makeText(requireContext(), "Lihat: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val prefs = requireContext().getSharedPreferences("store_pref", Context.MODE_PRIVATE)
        prefs.unregisterOnSharedPreferenceChangeListener(storePrefListener)
        _binding = null
    }
}