package com.example.miniproject.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.databinding.FragmentHomeBinding
import com.example.miniproject.model.Product
import com.google.android.material.slider.RangeSlider

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val allProducts = mutableListOf<Product>() // ✅ Data asli
    private val displayProducts = mutableListOf<Product>() // ✅ Data tampil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d("HomeFragment", "🟢 onCreateView dipanggil")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "🟢 onViewCreated mulai setup")

        setupRecyclerView()
        loadDummyProducts()
        setupSearch()
        setupFilter()
    }

    // ✅ PERBAIKAN: semua logika klik pindah ke sini
    private fun setupRecyclerView() {
        Log.d("HomeFragment", "🔧 setupRecyclerView() dipanggil")

        productAdapter = ProductAdapter(displayProducts, "user") { product, _ ->
            Log.d("HomeFragment", "🖱 Produk diklik: ${product.name}")

            // Pindah ke ProductDetailFragment
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

        binding.rvHomeProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvHomeProducts.adapter = productAdapter
        Log.d("HomeFragment", "✅ RecyclerView siap dengan adapter")
    }

    private fun loadDummyProducts() {
        Log.d("HomeFragment", "📦 Memuat produk dummy...")

        val dummy = listOf(
            Product(
                id = 1,
                name = "Cangkul Premium",
                price = 150000.0,
                description = "Cangkul baja berkualitas tinggi untuk mengolah tanah",
                imageUrl = null,
                imageResId = R.drawable.cangkul,
                categoryId = 1,
                stock = 10,
                categoryName = "Peralatan",
                createdAt = "2025-01-01"
            ),
            Product(
                id = 2,
                name = "Pupuk Organik 25kg",
                price = 200000.0,
                description = "Pupuk alami ramah lingkungan berkualitas premium",
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
                description = "Benih unggul padi hasil seleksi terbaik",
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
                description = "Traktor pertanian mini untuk lahan kecil dan menengah",
                imageUrl = null,
                imageResId = R.drawable.traktor,
                categoryId = 4,
                stock = 5,
                categoryName = "Peralatan",
                createdAt = "2025-01-01"
            ),
            // --- Produk Dummy Baru ---
            Product(
                id = 5,
                name = "Rotavator Tangan",
                price = 950000.0,
                description = "Alat putar penggembur tanah manual atau elektrik",
                imageUrl = null,
                imageResId = R.drawable.rotavator, // Pastikan resource ada
                categoryId = 1,
                stock = 7,
                categoryName = "Peralatan",
                createdAt = "2025-01-02"
            ),
            Product(
                id = 6,
                name = "Sekop Kebun",
                price = 85000.0,
                description = "Sekop multifungsi untuk menggali dan memindahkan material",
                imageUrl = null,
                imageResId = R.drawable.sekop, // Pastikan resource ada
                categoryId = 1,
                stock = 35,
                categoryName = "Peralatan",
                createdAt = "2025-01-02"
            ),
            Product(
                id = 7,
                name = "Selang Irigasi 20m",
                price = 120000.0,
                description = "Selang elastis kualitas premium untuk sistem penyiraman",
                imageUrl = null,
                imageResId = R.drawable.selang, // Pastikan resource ada
                categoryId = 1,
                stock = 40,
                categoryName = "Peralatan",
                createdAt = "2025-01-02"
            ),
            Product(
                id = 8,
                name = "Arit Tajam",
                price = 55000.0,
                description = "Arit berbahan baja untuk memotong rumput dan panen padi",
                imageUrl = null,
                imageResId = R.drawable.arit, // Pastikan resource ada
                categoryId = 1,
                stock = 20,
                categoryName = "Peralatan",
                createdAt = "2025-01-02"
            )
            // -------------------------
        )

        allProducts.clear()
        allProducts.addAll(dummy)
        displayProducts.clear()
        displayProducts.addAll(dummy)

        Log.d("HomeFragment", "✅ Produk dimuat: ${allProducts.size}")
        productAdapter.notifyDataSetChanged()
    }

    private fun setupSearch() {
        binding.etSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString().lowercase().trim()

                val filtered = if (text.isEmpty()) {
                    allProducts.toList()
                } else {
                    allProducts.filter {
                        it.name.lowercase().contains(text) ||
                                it.categoryName?.lowercase()?.contains(text) == true ||
                                it.description?.lowercase()?.contains(text) == true
                    }
                }

                displayProducts.clear()
                displayProducts.addAll(filtered)
                productAdapter.notifyDataSetChanged()

                Log.d("HomeFragment", "🔎 Search: \"$text\" → ${filtered.size} hasil")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilter() {
        binding.btnFilterHome.setOnClickListener {
            Log.d("HomeFragment", "⚙️ Tombol Filter diklik")
            val sliderView = layoutInflater.inflate(R.layout.dialog_price_filter, null)
            val slider = sliderView.findViewById<RangeSlider>(R.id.sliderPrice)

            val minPrice = allProducts.minOfOrNull { it.price } ?: 0.0
            val maxPrice = allProducts.maxOfOrNull { it.price } ?: 10000000.0

            slider.setValues(minPrice.toFloat(), maxPrice.toFloat())
            slider.valueFrom = minPrice.toFloat()
            slider.valueTo = maxPrice.toFloat()

            slider.addOnChangeListener { _, _, _ ->
                val values = slider.values
                Log.d("HomeFragment", "💰 Range slider: ${values[0]} - ${values[1]}")
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Filter Harga")
                .setView(sliderView)
                .setPositiveButton("Terapkan") { dialog, _ ->
                    val values = slider.values
                    val minPriceVal = values[0].toInt()
                    val maxPriceVal = values[1].toInt()

                    val filtered = allProducts.filter {
                        it.price.toInt() in minPriceVal..maxPriceVal
                    }

                    displayProducts.clear()
                    displayProducts.addAll(filtered)
                    productAdapter.notifyDataSetChanged()

                    Log.d("HomeFragment", "✅ Filter harga diterapkan: $minPriceVal - $maxPriceVal → ${filtered.size} hasil")
                    dialog.dismiss()
                }
                .setNegativeButton("Reset") { dialog, _ ->
                    displayProducts.clear()
                    displayProducts.addAll(allProducts)
                    productAdapter.notifyDataSetChanged()

                    Log.d("HomeFragment", "🔁 Filter direset → ${allProducts.size} hasil")
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("HomeFragment", "🧹 onDestroyView dipanggil")
    }
}
