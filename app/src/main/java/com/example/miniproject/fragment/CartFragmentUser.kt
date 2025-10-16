package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.CartAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.databinding.FragmentCartUserBinding
import java.text.NumberFormat
import java.util.*

class CartFragmentUser : Fragment() {
    private var _binding: FragmentCartUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        updateCartSummary()
        setupCheckoutButton()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            CartManager.cartItems,
            onQuantityChanged = { updateCartSummary() },
            onItemRemoved = {
                updateCartSummary()
                Toast.makeText(requireContext(), "Item dihapus dari keranjang", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        // Show/hide empty state
        if (CartManager.cartItems.isEmpty()) {
            binding.rvCart.visibility = View.GONE
            binding.layoutCartSummary.visibility = View.GONE
            binding.tvEmptyCart.visibility = View.VISIBLE
        } else {
            binding.rvCart.visibility = View.VISIBLE
            binding.layoutCartSummary.visibility = View.VISIBLE
            binding.tvEmptyCart.visibility = View.GONE
        }
    }

    private fun updateCartSummary() {
        val total = CartManager.getTotalPrice()
        val itemCount = CartManager.cartItems.sumOf { it.quantity }

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        binding.tvTotalItems.text = "Total ($itemCount item)"
        binding.tvTotalPrice.text = currencyFormat.format(total)

        // Update empty state
        if (CartManager.cartItems.isEmpty()) {
            binding.rvCart.visibility = View.GONE
            binding.layoutCartSummary.visibility = View.GONE
            binding.tvEmptyCart.visibility = View.VISIBLE
        }
    }

    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            if (CartManager.cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Keranjang kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Navigate to checkout
            try {
                findNavController().navigate(R.id.action_cartFragmentUser_to_checkoutActivity)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lanjut ke checkout", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}