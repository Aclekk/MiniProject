package com.example.miniproject.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.CartAdapter
import com.example.miniproject.adapter.OrderHistoryAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.databinding.FragmentCartBinding
import com.example.miniproject.ui.CheckoutActivity

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter

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
        setupOrderHistoryRecycler()

        updateCartSummary()

        binding.btnCheckout.setOnClickListener {
            if (CartManager.cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Keranjang kosong!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                intent.putExtra("product", CartManager.cartItems.first())
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

    private fun setupOrderHistoryRecycler() {
        orderHistoryAdapter = OrderHistoryAdapter(CartManager.orders) { order ->
            val detailFragment = OrderDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("orderId", order.id)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.rvOrderHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrderHistory.adapter = orderHistoryAdapter
    }

    private fun updateCartSummary() {
        val total = CartManager.cartItems.sumOf { it.price }
        binding.tvTotalCart.text = "Rp ${String.format("%,d", total.toInt())}"
        cartAdapter.notifyDataSetChanged()
        orderHistoryAdapter.notifyDataSetChanged()
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
