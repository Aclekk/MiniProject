package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.ProductAdapter
import com.example.miniproject.api.ApiClient
import com.example.miniproject.databinding.FragmentProductsBinding
import com.example.miniproject.model.Product
import com.example.miniproject.model.ProductListResponse // Import ini tidak dipakai dan bisa dihapus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.miniproject.model.ProductResponse

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter
    private val products = mutableListOf<Product>()
    private var userRole = ""

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
        loadProducts()
    }

    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "user") ?: "user"
        val username = sharedPref.getString("username", "User") ?: "User"

        // BARIS YANG MENYEBABKAN ERROR SUDAH DIHAPUS/DIKOMENTARI
        // binding.tvUserRole.text = "$userRole: $username"

        // FAB Add Product masih perlu dicek, karena FAB-nya masih ada di XML
        if (userRole == "admin") {
            binding.fabAddProduct.visibility = View.VISIBLE
        } else {
            binding.fabAddProduct.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(products, userRole) { product, action ->
            when (action) {
                "edit" -> editProduct(product)
                "delete" -> deleteProduct(product)
                "view" -> viewProduct(product)
            }
        }
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener { logout() }

        binding.fabAddProduct.setOnClickListener {
            // Asumsi AddProductFragment sudah kamu buat
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                // Karena AddProductFragment belum ada di kode yang kamu berikan,
                // aku ganti ke LoginFragment sementara (atau buat AddProductFragment)
                // Sebaiknya ganti dengan AddProductFragment() jika sudah siap
                .commit()
        }

        binding.swipeRefresh.setOnRefreshListener { loadProducts() }
    }

    private fun loadProducts() {
        showLoading(true)

        val call = ApiClient.apiService.getAllProducts()
        call.enqueue(object : retrofit2.Callback<ProductResponse> {
            override fun onResponse(call: retrofit2.Call<ProductResponse>, response: retrofit2.Response<ProductResponse>) {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    val productResponse = response.body()
                    if (productResponse?.success == true && productResponse.data != null) {
                        products.clear()
                        products.addAll(productResponse.data)
                        productAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), productResponse?.message ?: "Failed to load products", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load products: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ProductResponse>, t: Throwable) {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // ... (Fungsi editProduct, deleteProduct, viewProduct, showLoading, dan logout tetap sama)
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

    // Perlu tambahkan import untuk LoginFragment jika belum ada
    // import com.example.miniproject.fragment.LoginFragment // Tambahkan jika perlu

    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        // Ganti dengan class fragment yang sesuai jika kamu punya LoginFragment
        val loginFragment = LoginFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, loginFragment)
            .commit()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}