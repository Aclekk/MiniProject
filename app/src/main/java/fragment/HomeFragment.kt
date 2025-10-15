package com.example.miniproject.fragment

import android.content.Context
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
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.databinding.FragmentHomeBinding
import com.example.miniproject.model.Product
import com.example.miniproject.utils.ProductStorage

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        loadProducts() // ✅ Load data saat fragment dibuka
    }

    // ============================================================
    // 🧱 RECYCLER VIEW PRODUK
    // ============================================================
    private fun setupRecyclerView() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val userRole = sharedPref.getString("role", "user") ?: "user"

        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "edit" -> {
                    Toast.makeText(requireContext(), "Edit: ${product.name}", Toast.LENGTH_SHORT).show()
                    // TODO: Implementasi edit
                }
                "delete" -> {
                    // ✅ PENTING: Hapus dari storage juga!
                    deleteProduct(product)
                }
                "view" -> {
                    Toast.makeText(requireContext(), "Lihat: ${product.name}", Toast.LENGTH_SHORT).show()
                    // TODO: Implementasi detail view
                }
            }
        }

        binding.rvHomeProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    // ============================================================
    // 🗑️ DELETE PRODUCT
    // ============================================================
    private fun deleteProduct(product: Product) {
        // Hapus dari list
        allProducts.remove(product)
        products.remove(product)

        // Save ke storage
        ProductStorage.saveProducts(requireContext(), allProducts)

        // Update adapter
        productAdapter.notifyDataSetChanged()

        Toast.makeText(requireContext(), "Berhasil menghapus ${product.name}", Toast.LENGTH_SHORT).show()
    }

    // ============================================================
    // 🔹 CLICK HANDLERS
    // ============================================================
    private fun setupClickListeners() {
        binding.swipeRefreshHome.setOnRefreshListener {
            loadProducts()
        }

        // FAB untuk tambah produk (jika admin)
        binding.fabAddProductHome.setOnClickListener {
            Toast.makeText(requireContext(), "Tambah produk", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================================
    // 📦 LOAD PRODUK (STORAGE ATAU DUMMY)
    // ============================================================
    private fun loadProducts() {
        showLoading(true)
        Log.d(TAG, "🔄 Memulai loadProducts()")

        val context = requireContext()
        val savedProducts = ProductStorage.loadProducts(context)

        Log.d(TAG, "📊 Jumlah produk tersimpan: ${savedProducts.size}")

        // ✅ PENTING: Cek apakah data kosong, kalau iya load dummy
        if (savedProducts.isEmpty()) {
            Log.d(TAG, "⚠️ Data kosong! Loading dummy products...")

            // ✅ Pakai central dummy data
            val dummyProducts = com.example.miniproject.utils.DummyData.getDummyProducts()

            // Simpan dummy ke storage
            ProductStorage.saveProducts(context, dummyProducts)
            Log.d(TAG, "✅ ${dummyProducts.size} dummy products berhasil disimpan!")

            allProducts.clear()
            allProducts.addAll(dummyProducts)
        } else {
            Log.d(TAG, "✅ Menggunakan data tersimpan")
            allProducts.clear()
            allProducts.addAll(savedProducts)
        }

        // Update tampilan
        products.clear()
        products.addAll(allProducts)
        productAdapter.notifyDataSetChanged()

        Log.d(TAG, "🎉 Berhasil load ${products.size} produk")

        showLoading(false)
        binding.swipeRefreshHome.isRefreshing = false
    }

    // ============================================================
    // 🔍 SEARCH PRODUK
    // ============================================================
    private fun setupSearch() {
        binding.etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.lowercase().orEmpty()
                val filtered = allProducts.filter {
                    it.name.lowercase().contains(query) ||
                            it.categoryName?.lowercase()?.contains(query) == true
                }
                products.clear()
                products.addAll(filtered)
                productAdapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // ============================================================
    // 🌀 LOADING
    // ============================================================
    private fun showLoading(show: Boolean) {
        binding.progressBarHome.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // ✅ Reload setiap kali fragment ditampilkan
        loadProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}