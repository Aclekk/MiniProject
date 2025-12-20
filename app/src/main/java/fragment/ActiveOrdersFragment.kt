package com.example.miniproject.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.OrderHistoryAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Order
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentActiveOrdersBinding
import com.example.miniproject.firebase.MyFirebaseMessagingService
import com.example.miniproject.viewmodel.OrderViewModel
import com.example.miniproject.viewmodel.OrderViewModelFactory

class ActiveOrdersFragment : Fragment() {

    private var _binding: FragmentActiveOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderHistoryAdapter
    private val orders: MutableList<Order> = mutableListOf()

    // ✅ MVVM: ViewModel
    private lateinit var orderViewModel: OrderViewModel

    // ✅ BroadcastReceiver untuk auto-refresh
    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MyFirebaseMessagingService.ACTION_REFRESH_BUYER_ORDERS,
                MyFirebaseMessagingService.ACTION_ORDER_STATUS_CHANGED -> {
                    // ✅ Auto-refresh list saat dapat notifikasi
                    fetchBuyerOrders()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActiveOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ MVVM: Initialize ViewModel
        setupViewModel()

        adapter = OrderHistoryAdapter(
            orders = orders,
            onOrderClick = { order ->
                val detailFragment = OrderDetailFragment().apply {
                    arguments = Bundle().apply { putInt("orderId", order.id) }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            isActiveTab = true
        )

        binding.rvActiveOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveOrders.adapter = adapter

        fetchBuyerOrders()
    }

    // ================== MVVM SETUP ==================

    private fun setupViewModel() {
        val factory = OrderViewModelFactory(ApiClient.apiService)
        orderViewModel = ViewModelProvider(this, factory)[OrderViewModel::class.java]
        setupObservers()
    }

    private fun setupObservers() {
        // Observe buyer orders
        orderViewModel.buyerOrders.observe(viewLifecycleOwner) { orderList ->
            orders.clear()
            orders.addAll(orderList)
            adapter.notifyDataSetChanged()

            // ✅ PENTING: sync ke CartManager untuk OrderDetailFragment
            CartManager.orders.clear()
            CartManager.orders.addAll(orderList)

            Log.d("ActiveOrdersFragment", "✅ VM delivered ${orderList.size} orders")
        }

        // Observe error messages
        orderViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                orderViewModel.clearError()
            }
        }
    }

    // ================== LIFECYCLE & BROADCAST RECEIVER ==================

    override fun onStart() {
        super.onStart()
        // ✅ Register receiver untuk refresh realtime
        val filter = IntentFilter().apply {
            addAction(MyFirebaseMessagingService.ACTION_REFRESH_BUYER_ORDERS)
            addAction(MyFirebaseMessagingService.ACTION_ORDER_STATUS_CHANGED)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            requireContext().registerReceiver(refreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            requireContext().registerReceiver(refreshReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            requireContext().unregisterReceiver(refreshReceiver)
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        // ✅ Refresh setiap kali fragment visible
        fetchBuyerOrders()
    }

    // ================== FETCH ORDERS (MVVM) ==================

    private fun fetchBuyerOrders() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan, login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ MVVM: Fetch via ViewModel
        orderViewModel.fetchBuyerOrders(token, status = null)
    }

    // ================== CLEANUP ==================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}