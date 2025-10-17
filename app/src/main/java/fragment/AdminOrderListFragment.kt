package com.example.miniproject.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.OrderAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.databinding.FragmentActiveOrdersBinding

class AdminOrderListFragment : Fragment() {

    private var _binding: FragmentActiveOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔥 LOG DEBUGGING
        Log.d("AdminOrderList", "Total orders: ${CartManager.orders.size}")
        CartManager.orders.forEach { order ->
            Log.d("AdminOrderList", "Order #${order.id} - Status: ${order.status}")
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val activeOrders = CartManager.orders.filter { it.status != "Selesai" }

        // 🔥 LOG ACTIVE ORDERS
        Log.d("AdminOrderList", "Active orders count: ${activeOrders.size}")

        if (activeOrders.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada pesanan aktif", Toast.LENGTH_SHORT).show()
        }

        adapter = OrderAdapter(
            orders = activeOrders,
            onNextStatus = { order ->
                val oldStatus = order.status
                order.status = when (order.status) {
                    "Belum Bayar" -> "Dikemas"
                    "Dikemas" -> "Dikirim"
                    "Dikirim" -> "Selesai"
                    else -> order.status
                }

                Log.d("AdminOrderList", "Status changed: $oldStatus → ${order.status}")

                Toast.makeText(
                    requireContext(),
                    "Status diubah: $oldStatus → ${order.status}",
                    Toast.LENGTH_SHORT
                ).show()

                refreshList()
            }
        )

        binding.rvActiveOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveOrders.adapter = adapter

        // 🔥 LOG ADAPTER SET
        Log.d("AdminOrderList", "Adapter set with ${adapter.itemCount} items")
    }

    private fun refreshList() {
        val updatedOrders = CartManager.orders.filter { it.status != "Selesai" }

        adapter = OrderAdapter(
            orders = updatedOrders,
            onNextStatus = { order ->
                val oldStatus = order.status
                order.status = when (order.status) {
                    "Belum Bayar" -> "Dikemas"
                    "Dikemas" -> "Dikirim"
                    "Dikirim" -> "Selesai"
                    else -> order.status
                }

                Toast.makeText(
                    requireContext(),
                    "Status diubah: $oldStatus → ${order.status}",
                    Toast.LENGTH_SHORT
                ).show()

                refreshList()
            }
        )

        binding.rvActiveOrders.adapter = adapter
        Log.d("AdminOrderList", "List refreshed with ${adapter.itemCount} items")
    }

    override fun onResume() {
        super.onResume()
        Log.d("AdminOrderList", "onResume called")
        refreshList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}