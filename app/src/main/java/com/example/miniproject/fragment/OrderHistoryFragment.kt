package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.OrderHistoryAdapter
import com.example.miniproject.databinding.FragmentOrderHistoryBinding
import com.example.miniproject.model.DummyDataRepository
import com.example.miniproject.model.Order
import com.google.android.material.tabs.TabLayout

class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private val allOrders = DummyDataRepository.orders

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
    }

    private fun setupTabs() {
        setupRecyclerView(isActiveTab = true)
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Pesanan Aktif"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Riwayat Pesanan"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> setupRecyclerView(isActiveTab = true)
                    1 -> setupRecyclerView(isActiveTab = false)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView(isActiveTab: Boolean) {
        val filteredOrders = if (isActiveTab) {
            allOrders.filter { it.status != "Selesai" }.toMutableList()
        } else {
            allOrders.filter { it.status == "Selesai" }.toMutableList()
        }

        orderHistoryAdapter = OrderHistoryAdapter(filteredOrders, { order ->
            handleOrderStatusUpdate(order)
        }, isActiveTab)

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderHistoryAdapter
        }
    }

    private fun handleOrderStatusUpdate(order: Order) {
        val nextStatus = when (order.status) {
            "Menunggu Konfirmasi" -> "Dikirim"
            "Dikirim" -> "Selesai"
            else -> order.status
        }

        val orderInRepo = DummyDataRepository.orders.find { it.id == order.id }
        orderInRepo?.status = nextStatus

        Toast.makeText(requireContext(), "Status Order #${order.id} diubah menjadi '$nextStatus'", Toast.LENGTH_SHORT).show()

        val currentTab = binding.tabLayout.selectedTabPosition
        setupRecyclerView(isActiveTab = currentTab == 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
