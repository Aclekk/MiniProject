package com.example.miniproject.fragment

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentProductsBinding
import com.example.miniproject.model.Category
import com.example.miniproject.model.Product
import com.example.miniproject.data.model.PromoApi
import com.example.miniproject.viewmodel.HomeViewModel
import com.example.miniproject.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
    private val allProducts = mutableListOf<Product>()
    private var userRole: String = "buyer"

    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    private val promoList = mutableListOf<PromoApi>()
    private lateinit var promoAdapter: PromoUrlAdapter
    private var selectedPromoUri: Uri? = null
    private var currentPromoIndex: Int = 0

    // ✅ MVVM: ViewModel
    private lateinit var homeViewModel: HomeViewModel

    // 🎨 Animation handlers
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null

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

        // ✅ MVVM: Initialize ViewModel FIRST
        setupViewModel()

        getUserData()
        setupRecyclerView()
        setupCategoriesHorizontal()
        setupClickListeners()
        setupSearch()
        setupPromoCarousel()

        // ✅ MVVM: Load data via ViewModel
        loadBestSellerProducts()
        loadCategories()
        loadStoreInfo()
        loadPromos()

        // 🎨 START ANIMATIONS
        startInitialAnimations()
    }

    override fun onResume() {
        super.onResume()
        loadBestSellerProducts()
        loadCategories()
        loadStoreInfo()
        loadPromos()
        startAutoScrollPromo()
    }

    override fun onPause() {
        super.onPause()
        stopAutoScrollPromo()
    }

    // ================== MVVM SETUP ==================

    private fun setupViewModel() {
        val factory = HomeViewModelFactory(ApiClient.apiService)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        setupObservers()
    }

    private fun setupObservers() {
        // Observe products
        homeViewModel.products.observe(viewLifecycleOwner) { productList ->
            allProducts.clear()
            allProducts.addAll(productList)

            products.clear()
            products.addAll(productList)

            productAdapter.notifyDataSetChanged()

            // 🎨 Animate products after loading
            binding.progressBar.visibility = View.GONE
            animateProductGrid()

            Log.d("ProductsFragment", "✅ VM delivered ${productList.size} products")
        }

        // Observe categories
        homeViewModel.categories.observe(viewLifecycleOwner) { categoryList ->
            categories.clear()
            categories.addAll(categoryList)
            categoryAdapter.notifyDataSetChanged()

            // 🎨 Animate categories after loading
            animateCategoryItems()

            Log.d("ProductsFragment", "✅ VM delivered ${categoryList.size} categories")

            if (categoryList.isEmpty()) {
                Toast.makeText(requireContext(), "Belum ada kategori", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe store info
        homeViewModel.storeInfo.observe(viewLifecycleOwner) { storeData ->
            binding.tvStoreName.text = "🌾 ${storeData.appName}"
            binding.tvStoreTagline.text = storeData.appTagline

            if (storeData.appLogo.isNotEmpty()) {
                val logoUrl = ApiClient.getImageUrl(storeData.appLogo)
                Glide.with(this@ProductsFragment)
                    .load(logoUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.imgStoreLogo)
            } else {
                binding.imgStoreLogo.setImageResource(R.drawable.ic_person)
            }

            binding.tvAboutTagline.text = storeData.appTagline
            binding.tvAboutAddress.text = "📍 ${storeData.appAddress}"
            binding.tvAboutPhone.text = "📞 ${storeData.contactPhone}"
            binding.tvAboutEmail.text = "📧 ${storeData.contactEmail}"
        }

        // Observe promos
        homeViewModel.promos.observe(viewLifecycleOwner) { promoList ->
            this.promoList.clear()
            this.promoList.addAll(promoList)
            promoAdapter.notifyDataSetChanged()
            currentPromoIndex = 0

            // 🎨 Start auto scroll after loading promos
            startAutoScrollPromo()
        }

        // Observe loading state
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        homeViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                homeViewModel.clearError()
            }
        }

        // Observe success messages
        homeViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                homeViewModel.clearSuccess()
            }
        }
    }

    // ================== 🎨 ANIMATIONS ==================

    private fun startInitialAnimations() {
        binding.root.alpha = 0f

        binding.root.animate()
            .alpha(1f)
            .setDuration(400)
            .start()

        animateHeaderCard()
        animateSearchBar()
        animatePromoCarousel()
    }

    private fun animateHeaderCard() {
        binding.root.post {
            val headerCard = (binding.root as? ViewGroup)?.getChildAt(0)

            headerCard?.let { card ->
                card.alpha = 0f
                card.translationY = -100f

                card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(100)
                    .start()
            }
        }
    }

    private fun animateSearchBar() {
        binding.root.post {
            val mainLayout = (binding.root as? ViewGroup)?.getChildAt(0) as? ViewGroup
            val searchBar = mainLayout?.getChildAt(1)

            searchBar?.let { bar ->
                bar.alpha = 0f
                bar.scaleX = 0.9f
                bar.scaleY = 0.9f

                bar.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setStartDelay(200)
                    .start()
            }
        }
    }

    private fun animatePromoCarousel() {
        binding.viewPagerPromo.alpha = 0f
        binding.viewPagerPromo.scaleX = 0.95f
        binding.viewPagerPromo.scaleY = 0.95f

        binding.viewPagerPromo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setStartDelay(300)
            .start()
    }

    private fun animateCategoryItems() {
        binding.rvCategories.post {
            for (i in 0 until binding.rvCategories.childCount) {
                val child = binding.rvCategories.getChildAt(i)
                child.alpha = 0f
                child.translationX = -50f

                child.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setStartDelay(400 + (i * 80L))
                    .setDuration(500)
                    .start()
            }
        }
    }

    private fun animateProductGrid() {
        binding.rvProducts.post {
            for (i in 0 until binding.rvProducts.childCount) {
                val child = binding.rvProducts.getChildAt(i)
                child.alpha = 0f
                child.translationY = 80f
                child.scaleX = 0.9f
                child.scaleY = 0.9f

                child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(500 + (i * 60L))
                    .setDuration(500)
                    .start()
            }
        }
    }

    private fun startAutoScrollPromo() {
        if (promoList.isEmpty()) return

        stopAutoScrollPromo()

        autoScrollRunnable = object : Runnable {
            override fun run() {
                if (_binding == null || !isAdded) return

                val nextIndex = (currentPromoIndex + 1) % promoList.size
                binding.viewPagerPromo.setCurrentItem(nextIndex, true)

                autoScrollHandler.postDelayed(this, 4000)
            }
        }

        autoScrollHandler.postDelayed(autoScrollRunnable!!, 4000)
    }

    private fun stopAutoScrollPromo() {
        autoScrollRunnable?.let {
            autoScrollHandler.removeCallbacks(it)
        }
        autoScrollRunnable = null
    }

    private fun animateButtonPress(view: View) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
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
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    for (i in 0 until childCount) {
                        val child = getChildAt(i)
                        if (child != null && child.alpha < 1f) {
                            child.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(400)
                                .start()
                        }
                    }
                }
            })
        }
    }

    // ================== HORIZONTAL CATEGORIES ==================

    private fun setupCategoriesHorizontal() {
        categoryAdapter = CategoryAdapter(
            categories = categories,
            userRole = userRole,
            onItemClick = { category ->
                val bundle = Bundle().apply {
                    putInt("category_id", category.id)
                    putString("category_name", category.categoryName)
                }
                val fragment = CategoriesFragment().apply {
                    arguments = bundle
                }
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onEditClick = null,
            onDeleteClick = null
        )

        val layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvCategories.layoutManager = layoutManager

        val snapHelper = LinearSnapHelper()
        try {
            snapHelper.attachToRecyclerView(binding.rvCategories)
        } catch (e: IllegalStateException) {
            // Already attached
        }

        val spacingInPixels = 16
        binding.rvCategories.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect.right = spacingInPixels
                if (position == 0) {
                    outRect.left = spacingInPixels
                }
            }
        })

        binding.rvCategories.overScrollMode = View.OVER_SCROLL_NEVER
        binding.rvCategories.adapter = categoryAdapter

        binding.rvCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i)
                    if (child != null && child.alpha < 1f) {
                        child.animate()
                            .alpha(1f)
                            .translationX(0f)
                            .setDuration(300)
                            .start()
                    }
                }
            }
        })
    }

    // ================== CLICK LISTENERS ==================

    private fun setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            // ✅ MVVM: Refresh all data via ViewModel
            homeViewModel.loadBestSellerProducts()
            homeViewModel.loadCategories()
            homeViewModel.loadStoreInfo()
            homeViewModel.loadPromos()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnFilter.setOnClickListener {
            animateButtonPress(it)
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

        binding.rvProducts.postDelayed({
            animateProductGrid()
        }, 100)

        if (filtered.isEmpty()) {
            Toast.makeText(context, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    // ================== LOAD DATA (MVVM) ==================

    private fun loadBestSellerProducts() {
        // ✅ MVVM: Trigger load from ViewModel
        homeViewModel.loadBestSellerProducts()
    }

    private fun loadCategories() {
        // ✅ MVVM: Trigger load from ViewModel
        homeViewModel.loadCategories()
    }

    private fun loadStoreInfo() {
        // ✅ MVVM: Trigger load from ViewModel
        homeViewModel.loadStoreInfo()
    }

    private fun loadPromos() {
        // ✅ MVVM: Trigger load from ViewModel
        homeViewModel.loadPromos()
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
            animateButtonPress(it)
            promoImagePicker.launch("image/*")
        }

        binding.btnDeletePromo.setOnClickListener {
            animateButtonPress(it)

            if (promoList.isEmpty()) {
                Toast.makeText(requireContext(), "Belum ada promo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idx = currentPromoIndex.coerceIn(0, promoList.lastIndex)
            val promoId = promoList[idx].id

            deletePromo(promoId)
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

                // ✅ MVVM: Upload via ViewModel
                homeViewModel.uploadPromo(token, part)

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

        // ✅ MVVM: Delete via ViewModel
        homeViewModel.deletePromo(token, promoId)
    }

    // ================== CLEANUP ==================

    override fun onDestroyView() {
        stopAutoScrollPromo()
        _binding = null
        super.onDestroyView()
    }
}