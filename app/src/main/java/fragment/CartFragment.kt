package com.example.miniproject.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.CartAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.databinding.FragmentCartBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.example.miniproject.ui.CheckoutActivity

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCartRecycler()
        setupOrderTabs()
        updateCartSummary()

        binding.btnCheckout.setOnClickListener {
            if (CartManager.cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            } else {
                // 🆕 FIX: Kirim flag "from_cart" ke CheckoutActivity
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                intent.putExtra("product", CartManager.cartItems.first())
                intent.putExtra("quantity", 1)
                intent.putExtra("from_cart", true) // ✅ Tambahkan flag ini
                startActivity(intent)
            }
        }
    }

    private fun setupCartRecycler() {
        cartAdapter = CartAdapter(CartManager.cartItems) { product ->
            CartManager.cartItems.remove(product)
            updateCartSummary()
        }
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter
    }

    private fun setupOrderTabs() {
        val tabLayout = binding.tabLayoutOrders
        val viewPager = binding.viewPagerOrders

        viewPager.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) ActiveOrdersFragment() else CompletedOrdersFragment()
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Pesanan Aktif" else "Riwayat Pesanan"
        }.attach()
    }

    private fun updateCartSummary() {
        val total = CartManager.cartItems.sumOf { it.price }
        binding.tvTotalCart.text = "Rp ${String.format("%,d", total.toInt())}"
        cartAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        updateCartSummary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}