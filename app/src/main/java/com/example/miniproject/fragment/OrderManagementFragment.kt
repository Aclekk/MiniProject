package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.OrderManagementAdapter
import com.example.miniproject.data.OrderManager
import com.example.miniproject.databinding.FragmentOrderManagementBinding
import com.example.miniproject.model.Order

class OrderManagementFragment : Fragment() {

    private var _binding: FragmentOrderManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderManagementAdapter
    private var currentFilter: String = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterButtons()
        loadOrders()
    }

    private fun setupRecyclerView() {
        adapter = OrderManagementAdapter(
            orders = mutableListOf(),
            onStatusChangeClick = { order ->
                updateOrderStatus(order)
            }
        )

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }

    private fun setupFilterButtons() {
        binding.btnFilterPending.setOnClickListener {
            currentFilter = "Menunggu Konfirmasi"
            loadOrders()
        }

        binding.btnFilterPacked.setOnClickListener {
            currentFilter = "Dikemas"
            loadOrders()
        }

        binding.btnFilterShipped.setOnClickListener {
            currentFilter = "Dikirim"
            loadOrders()
        }

        binding.btnFilterAll.setOnClickListener {
            currentFilter = "Semua"
            loadOrders()
        }
    }

    private fun loadOrders() {
        val allOrders = OrderManager.allOrders

        val filteredOrders = if (currentFilter == "Semua") {
            allOrders
        } else {
            allOrders.filter { it.status == currentFilter }
        }

        adapter.updateData(filteredOrders)

        // Update empty state
        if (filteredOrders.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvOrders.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvOrders.visibility = View.VISIBLE
        }
    }

    private fun updateOrderStatus(order: Order) {
        val newStatus = when (order.status) {
            "Menunggu Konfirmasi" -> "Dikemas"
            "Dikemas" -> "Dikirim"
            "Dikirim" -> "Selesai"
            else -> order.status
        }

        OrderManager.updateOrderStatus(order.id, newStatus)

        Toast.makeText(
            requireContext(),
            "Status diubah ke: $newStatus",
            Toast.LENGTH_SHORT
        ).show()

        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}