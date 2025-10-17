package com.example.miniproject.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.MainActivity
import com.example.miniproject.R
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.adapter.PromoAdapter
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.data.ProductDataSource
import com.example.miniproject.databinding.FragmentProductsBinding
import com.example.miniproject.model.Product
import com.example.miniproject.model.Promo
import com.example.miniproject.model.StoreProfile

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private lateinit var promoAdapter: PromoAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()
    private var userRole = ""

    companion object {
        // List produk global sementara (di-reset tiap restart)
        val globalProductList = mutableListOf<Product>()
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
        updateStoreInfo() // ✅ Load info toko saat fragment dibuat
        setupCarousel()
        setupCategories()
        setupRecyclerView()
        setupClickListeners() // ✅ Fungsi ini sekarang sudah ada di bawah
        setupSearchAndFilter()
        loadDummyProducts()
    }

    // ✅ Update otomatis saat fragment kembali aktif
    override fun onResume() {
        super.onResume()
        updateStoreInfo()
        Log.d("ProductsFragment", "🔄 onResume called - Store info refreshed")
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
            Promo(
                1, "Diskon 30% Pupuk Organik",
                "Dapatkan pupuk organik berkualitas dengan harga spesial", R.drawable.karosel1
            ),
            Promo(
                2, "Beli Cangkul Gratis Sekop",
                "Penawaran terbatas untuk alat pertanian pilihan", R.drawable.karosel2
            ),
            Promo(
                3, "Flash Sale Benih Padi",
                "Harga spesial untuk pembelian dalam jumlah banyak", R.drawable.karosel3
            )
        )
        promoAdapter = PromoAdapter(promos)
        binding.viewPagerPromo.adapter = promoAdapter
        binding.dotsIndicator.attachTo(binding.viewPagerPromo)
    }

    // ============================================================
    // 🔹 KATEGORI PRODUK - FIXED ✅
    // ============================================================
    private fun setupCategories() {
        val categories = CategoryRepository.getCategories()

        categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            // ✅ Gunakan fungsi di MainActivity untuk navigasi ke Categories
            (activity as? MainActivity)?.openCategoriesWithFilter(
                categoryId = selectedCategory.id,
                categoryName = selectedCategory.categoryName
            )
        }

        val horizontalLayout =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvCategories.apply {
            layoutManager = horizontalLayout
            adapter = categoryAdapter
            setHasFixedSize(true)
            clipToPadding = false
            isNestedScrollingEnabled = false
        }
    }

    // ============================================================
    // 🔹 RECYCLER VIEW PRODUK (Grid)
    // ============================================================
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "edit" -> editProduct(product)
                "delete" -> deleteProduct(product)
                "view" -> viewProduct(product)
            }
        }

        val gridLayout = GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)

        binding.rvProducts.apply {
            layoutManager = gridLayout
            adapter = productAdapter
            setHasFixedSize(true)
            clipToPadding = false
            isNestedScrollingEnabled = false
        }
    }

    // ============================================================
    // 🔹 CLICK HANDLERS - ADDED BACK ✅
    // ============================================================
    private fun setupClickListeners() {
        binding.fabAddProduct.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddProductFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadDummyProducts()
        }
    }

    // ============================================================
    // 🔹 LOAD DUMMY DATA PRODUK
    // ============================================================
    private fun loadDummyProducts() {
        showLoading(true)

        // ✅ Gunakan data dari ProductDataSource
        ProductDataSource.loadDummyData()
        val allData = ProductDataSource.getAllProducts()

        globalProductList.clear()
        globalProductList.addAll(allData)

        products.clear()
        products.addAll(globalProductList)

        allProducts.clear()
        allProducts.addAll(globalProductList)

        productAdapter.notifyDataSetChanged()

        showLoading(false)
        binding.swipeRefresh.isRefreshing = false
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
                Log.d("ProductsFragment", "🔎 Search: \"$searchText\" → ${filtered.size} hasil")
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
                    Toast.makeText(
                        requireContext(),
                        "Filter: $selected (${filtered.size} produk)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Tampilkan Semua") { dialog, _ ->
                    products.clear()
                    products.addAll(allProducts)
                    productAdapter.notifyDataSetChanged()
                    Toast.makeText(
                        requireContext(),
                        "Menampilkan semua produk (${allProducts.size})",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                .create()
            dialog.show()
        }
    }

    // ============================================================
    // 🔹 UTILITAS PRODUK
    // ============================================================
    private fun filterByCategory(category: String) {
        val filtered = ProductDataSource.getProductsByCategory(category)
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
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin mau hapus ${product.name}?")
            .setPositiveButton("Ya") { _, _ ->
                globalProductList.removeAll { it.id == product.id }
                products.clear()
                products.addAll(globalProductList)
                productAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "✅ Produk dihapus!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun viewProduct(product: Product) {
        val detailFragment = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable("product", product)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
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

    // ============================================================
    // 🔹 UPDATE STORE INFO
    // ============================================================
    fun updateStoreInfo() {
        val updatedName = StoreProfile.storeName
        val updatedContact = StoreProfile.storeContact
        val updatedAddress = StoreProfile.storeAddress
        val updatedAbout = StoreProfile.storeAbout
        val updatedPhoto = StoreProfile.storePhotoUri

        view?.let {
            it.findViewById<TextView>(R.id.tvStoreName)?.text = updatedName
            it.findViewById<TextView>(R.id.tvStoreContact)?.text = updatedContact
            it.findViewById<TextView>(R.id.tvStoreAddress)?.text = updatedAddress
            it.findViewById<TextView>(R.id.tvStoreAbout)?.text = updatedAbout

            val imgLogo = it.findViewById<ImageView>(R.id.imgStoreLogo)
            if (updatedPhoto != null) {
                imgLogo?.setImageURI(updatedPhoto)
                Log.d("ProductsFragment", "🟢 Updated store photo: $updatedPhoto")
            } else {
                imgLogo?.setImageResource(R.drawable.ic_person)
                Log.d("ProductsFragment", "⚪ Reset to default photo")
            }
        }
    }

    // ============================================================
    // 🔹 PUBLIC FUNCTIONS
    // ============================================================
    fun addProduct(product: Product) {
        globalProductList.add(product)
        products.clear()
        products.addAll(globalProductList)
        productAdapter.notifyDataSetChanged()
    }

    fun updateProduct(updated: Product) {
        val index = globalProductList.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            globalProductList[index] = updated
            products.clear()
            products.addAll(globalProductList)
            productAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}