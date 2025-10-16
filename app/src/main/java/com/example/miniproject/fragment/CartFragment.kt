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
import com.example.miniproject.adapter.OrderAdapter
import com.example.miniproject.databinding.FragmentCartBinding
import com.example.miniproject.data.CartManager

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderAdapter
    private var userRole: String = "user"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserData()
        setupRecyclerView()
        loadOrders()
    }

    // 🔹 Ambil role user dari SharedPreferences
    private fun getUserData() {
        val sharedPref = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("role", "user") ?: "user"
    }

    // 🔹 Setup RecyclerView
    private fun setupRecyclerView() {
        adapter = OrderAdapter(userRole) { order ->
            if (userRole == "admin") {
                when (order.status) {
                    "Menunggu Konfirmasi" -> {
                        CartManager.updateOrderStatus(order.id, "Dikemas")
                        Toast.makeText(requireContext(), "Pesanan dikemas 📦", Toast.LENGTH_SHORT).show()
                    }
                    "Dikemas" -> {
                        CartManager.updateOrderStatus(order.id, "Dikirim")
                        Toast.makeText(requireContext(), "Pesanan dikirim 🚚", Toast.LENGTH_SHORT).show()
                    }
                    "Dikirim" -> {
                        CartManager.updateOrderStatus(order.id, "Selesai")
                        Toast.makeText(requireContext(), "Pesanan selesai ✅", Toast.LENGTH_SHORT).show()
                    }
                    "Selesai" -> {
                        Toast.makeText(requireContext(), "Pesanan sudah selesai", Toast.LENGTH_SHORT).show()
                    }
                }
                loadOrders()
            }
        }

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }

    // 🔹 Load daftar pesanan
    private fun loadOrders() {
        val orders = CartManager.getOrders()
        if (orders.isEmpty()) {
            Toast.makeText(requireContext(), "Belum ada pesanan", Toast.LENGTH_SHORT).show()
        }
        adapter.setData(orders)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}