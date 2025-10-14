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

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var promoAdapter: PromoAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()
    private var userRole = ""

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
        setupCarousel()
        setupCategories()
        setupRecyclerView()
        setupClickListeners()
        setupSearchAndFilter()
        loadDummyProducts()
    }

    // ============================================================
    // 🔹 LOGIN ROLE
    // ============================================================
    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "user") ?: "user"
        binding.fabAddProduct.visibility =
            if (userRole == "admin") View.VISIBLE else View.GONE
    }

    // ============================================================
    // 🔹 PROMO CAROUSEL
    // ============================================================
    private fun setupCarousel() {
        val promos = listOf(
            Promo(1, "Diskon 30% Pupuk Organik",
                "Dapatkan pupuk organik berkualitas dengan harga spesial", R.drawable.promo1),
            Promo(2, "Beli Cangkul Gratis Sekop",
                "Penawaran terbatas untuk alat pertanian pilihan", R.drawable.promo2),
            Promo(3, "Flash Sale Benih Padi",
                "Harga spesial untuk pembelian dalam jumlah banyak", R.drawable.promo3)
        )
        promoAdapter = PromoAdapter(promos)
        binding.viewPagerPromo.adapter = promoAdapter
        binding.dotsIndicator.attachTo(binding.viewPagerPromo)
    }

    // ============================================================
    // 🔹 KATEGORI PRODUK
    // ============================================================
    private fun setupCategories() {
        val categories = listOf(
            Category(1, "Peralatan", "2025-01-01"),
            Category(2, "Pupuk", "2025-01-01"),
            Category(3, "Benih", "2025-01-01"),
            Category(4, "Alat Pertanian", "2025-01-01")
        )

        categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            filterByCategory(selectedCategory.categoryName)
        }

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    // ============================================================
    // 🔹 RECYCLER VIEW PRODUK (Grid 4 kolom ke bawah)
    // ============================================================
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "edit" -> editProduct(product)
                "delete" -> deleteProduct(product)
                "view" -> viewProduct(product)
            }
        }

        val gridLayout = GridLayoutManager(requireContext(), 4, RecyclerView.VERTICAL, false)

        binding.rvProducts.apply {
            layoutManager = gridLayout
            adapter = productAdapter
            setHasFixedSize(true)
            clipToPadding = false
            isNestedScrollingEnabled = false
        }
    }

    // ============================================================
    // 🔹 CLICK HANDLERS
    // ============================================================
    private fun setupClickListeners() {
        binding.fabAddProduct.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddProductFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.swipeRefresh.setOnRefreshListener { loadDummyProducts() }
    }

    // ============================================================
    // 🔹 LOAD DUMMY DATA PRODUK
    // ============================================================
    private fun loadDummyProducts() {
        showLoading(true)

        val dummyProducts = listOf(
            Product(
                id = 1,
                name = "Cangkul Premium",
                price = 150000.0,
                description = "Cangkul berkualitas tinggi untuk mengolah tanah",
                imageUrl = null,
                categoryId = 1,
                stock = 50,
                categoryName = "Peralatan",
                createdAt = "2025-01-01",
                imageResId = R.drawable.cangkul
            ),
            Product(
                id = 2,
                name = "Pupuk Organik 25kg",
                price = 200000.0,
                description = "Pupuk organik alami berkualitas tinggi",
                imageUrl = null,
                categoryId = 2,
                stock = 30,
                categoryName = "Pupuk",
                createdAt = "2025-01-01",
                imageResId = R.drawable.pupuk
            ),
            Product(
                id = 3,
                name = "Benih Padi Premium",
                price = 50000.0,
                description = "Benih padi unggul hasil seleksi",
                imageUrl = null,
                categoryId = 3,
                stock = 100,
                categoryName = "Benih",
                createdAt = "2025-01-01",
                imageResId = R.drawable.benih
            ),
            Product(
                id = 4,
                name = "Traktor Mini",
                price = 5000000.0,
                description = "Traktor mini untuk pertanian skala kecil",
                imageUrl = null,
                categoryId = 4,
                stock = 5,
                categoryName = "Alat Pertanian",
                createdAt = "2025-01-01",
                imageResId = R.drawable.traktor
            )
        )

        allProducts.clear()
        allProducts.addAll(dummyProducts)
        products.clear()
        products.addAll(dummyProducts)
        productAdapter.notifyDataSetChanged()
        showLoading(false)
        binding.swipeRefresh.isRefreshing = false
        Toast.makeText(requireContext(), "Produk dimuat", Toast.LENGTH_SHORT).show()
    }

    // ============================================================
    // 🔹 SEARCH & FILTER
    // ============================================================
    private fun setupSearchAndFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.lowercase().orEmpty()
                val filtered = allProducts.filter {
                    it.name.lowercase().contains(searchText) ||
                            it.categoryName?.lowercase()?.contains(searchText) == true
                }
                products.clear()
                products.addAll(filtered)
                productAdapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnFilter.setOnClickListener {
            val categories = allProducts.mapNotNull { it.categoryName }.distinct()
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Filter Produk")
                .setItems(categories.toTypedArray()) { _, which ->
                    val selected = categories[which]
                    val filtered = allProducts.filter { it.categoryName == selected }
                    products.clear()
                    products.addAll(filtered)
                    productAdapter.notifyDataSetChanged()
                }
                .setNegativeButton("Tampilkan Semua") { _, _ ->
                    products.clear()
                    products.addAll(allProducts)
                    productAdapter.notifyDataSetChanged()
                }
                .create()
            dialog.show()
        }
    }

    // ============================================================
    // 🔹 UTILITAS
    // ============================================================
    private fun filterByCategory(category: String) {
        val filtered = allProducts.filter { it.categoryName == category }
        products.clear()
        products.addAll(filtered)
        productAdapter.notifyDataSetChanged()
    }

    private fun editProduct(product: Product) {
        Toast.makeText(requireContext(), "Edit: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteProduct(product: Product) {
        Toast.makeText(requireContext(), "Delete: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun viewProduct(product: Product) {
        Toast.makeText(requireContext(), "View: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()
        Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
