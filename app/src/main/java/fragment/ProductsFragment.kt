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
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.adapter.PromoAdapter
import com.example.miniproject.databinding.FragmentProductsBinding
import com.example.miniproject.model.Product
import com.example.miniproject.model.Promo

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var promoAdapter: PromoAdapter
    private val products = mutableListOf<Product>()
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
        setupRecyclerView()
        setupClickListeners()
        setupSearchAndFilter()
        loadDummyProducts()
    }

    // Ambil data role user
    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "user") ?: "user"
        binding.fabAddProduct.visibility =
            if (userRole == "admin") View.VISIBLE else View.GONE
    }

    // Carousel promo dummy
    private fun setupCarousel() {
        val promos = listOf(
            Promo(
                id = 1,
                title = "Diskon 30% Pupuk Organik",
                description = "Dapatkan pupuk organik berkualitas dengan harga spesial",
                imageResId = R.drawable.promo1
            ),
            Promo(
                id = 2,
                title = "Beli Cangkul Gratis Sekop",
                description = "Penawaran terbatas untuk alat pertanian pilihan",
                imageResId = R.drawable.promo2
            ),
            Promo(
                id = 3,
                title = "Flash Sale Benih Padi",
                description = "Harga spesial untuk pembelian dalam jumlah banyak",
                imageResId = R.drawable.promo3
            )
        )
        promoAdapter = PromoAdapter(promos)
        binding.viewPagerPromo.adapter = promoAdapter
        binding.dotsIndicator.attachTo(binding.viewPagerPromo)
    }

    // RecyclerView setup
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "edit" -> editProduct(product)
                "delete" -> deleteProduct(product)
                "view" -> viewProduct(product)
            }
        }

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
            clipToPadding = false
            setPadding(16, 16, 16, 16)
        }
    }

    // Listener tombol
    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener { logout() }

        binding.fabAddProduct.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddProductFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.swipeRefresh.setOnRefreshListener { loadDummyProducts() }
    }

    // Load dummy data produk
    private fun loadDummyProducts() {
        showLoading(true)

        val dummyProducts = listOf(
            Product(
                id = 1,
                name = "Cangkul Premium",
                price = 150000.0,
                description = "Cangkul berkualitas tinggi untuk mengolah tanah",
                imageUrl = null,
                imageResId = R.drawable.cangkul,
                categoryId = 1,
                stock = 50,
                categoryName = "Pertanian",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 2,
                name = "Pupuk Organik 25kg",
                price = 200000.0,
                description = "Pupuk organik alami berkualitas tinggi",
                imageUrl = null,
                imageResId = R.drawable.pupuk,
                categoryId = 2,
                stock = 30,
                categoryName = "Pupuk",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 3,
                name = "Benih Padi Premium",
                price = 50000.0,
                description = "Benih padi unggul hasil seleksi",
                imageUrl = null,
                imageResId = R.drawable.benih,
                categoryId = 3,
                stock = 100,
                categoryName = "Benih",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 4,
                name = "Traktor Mini",
                price = 5000000.0,
                description = "Traktor mini untuk pertanian skala kecil",
                imageUrl = null,
                imageResId = R.drawable.traktor,
                categoryId = 4,
                stock = 5,
                categoryName = "Peralatan",
                createdAt = "2025-01-01"
            )
        )

        products.clear()
        products.addAll(dummyProducts)
        productAdapter.notifyDataSetChanged()
        showLoading(false)
        binding.swipeRefresh.isRefreshing = false
        Toast.makeText(requireContext(), "Products loaded", Toast.LENGTH_SHORT).show()
    }

    // SEARCH & FILTER
    private fun setupSearchAndFilter() {
        // SEARCH listener
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString()?.lowercase().orEmpty()
                val filtered = products.filter {
                    it.name?.lowercase()?.contains(searchText) == true ||
                            it.categoryName?.lowercase()?.contains(searchText) == true
                }
                productAdapter.updateList(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // FILTER kategori
        binding.btnFilter.setOnClickListener {
            val categories = products.mapNotNull { it.categoryName }.distinct()
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Filter Produk")
                .setItems(categories.toTypedArray()) { _, which ->
                    val selected = categories[which]
                    val filtered = products.filter { it.categoryName == selected }
                    productAdapter.updateList(filtered)
                }
                .setNegativeButton("Tampilkan Semua") { _, _ ->
                    productAdapter.updateList(products)
                }
                .create()
            dialog.show()
        }
    }

    // Edit / Delete / View actions
    private fun editProduct(product: Product) {
        Toast.makeText(requireContext(), "Edit: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteProduct(product: Product) {
        Toast.makeText(requireContext(), "Delete: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    private fun viewProduct(product: Product) {
        Toast.makeText(requireContext(), "View: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    // Loading progress
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    // Logout user
    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
