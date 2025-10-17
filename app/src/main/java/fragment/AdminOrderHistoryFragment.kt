package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.OrderAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.databinding.FragmentCompletedOrdersBinding

class AdminOrderHistoryFragment : Fragment() {

    private var _binding: FragmentCompletedOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val completedOrders = CartManager.orders.filter { it.status == "Selesai" }

        adapter = OrderAdapter(
            orders = completedOrders,
            onNextStatus = null // Tidak ada aksi untuk pesanan selesai
        )

        binding.rvCompletedOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCompletedOrders.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Refresh data
        val updatedOrders = CartManager.orders.filter { it.status == "Selesai" }
        adapter = OrderAdapter(
            orders = updatedOrders,
            onNextStatus = null
        )
        binding.rvCompletedOrders.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}