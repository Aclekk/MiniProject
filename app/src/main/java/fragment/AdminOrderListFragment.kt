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
import com.example.miniproject.adapter.OrderAdapter
import com.example.miniproject.data.Order
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentAdminOrderListBinding
import com.example.miniproject.firebase.MyFirebaseMessagingService
import com.example.miniproject.util.nextStatusForSeller
import com.example.miniproject.viewmodel.OrderViewModel
import com.example.miniproject.viewmodel.OrderViewModelFactory

class AdminOrderListFragment : Fragment() {

    private var _binding: FragmentAdminOrderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderAdapter: OrderAdapter
    private val orders: MutableList<Order> = mutableListOf()

    // ✅ MVVM: ViewModel (SHARED dengan ActiveOrders)
    private lateinit var orderViewModel: OrderViewModel

    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MyFirebaseMessagingService.ACTION_REFRESH_ORDERS,
                MyFirebaseMessagingService.ACTION_ORDER_STATUS_CHANGED -> {
                    fetchSellerOrders()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ MVVM: Initialize ViewModel
        setupViewModel()

        setupRecyclerView()
        fetchSellerOrders()
    }

    // ================== MVVM SETUP ==================

    private fun setupViewModel() {
        val factory = OrderViewModelFactory(ApiClient.apiService)
        orderViewModel = ViewModelProvider(this, factory)[OrderViewModel::class.java]
        setupObservers()
    }

    private fun setupObservers() {
        // Observe seller orders
        orderViewModel.sellerOrders.observe(viewLifecycleOwner) { orderList ->
            orderAdapter.replaceAll(orderList)
            Log.d("AdminOrderListFragment", "✅ VM delivered ${orderList.size} orders")
        }

        // Observe success messages
        orderViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                orderViewModel.clearSuccess()
            }
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
        val filter = IntentFilter().apply {
            addAction(MyFirebaseMessagingService.ACTION_REFRESH_ORDERS)
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
        fetchSellerOrders()
    }

    // ================== RECYCLERVIEW SETUP ==================

    private fun setupRecyclerView() {
        binding.rvAdminOrders.layoutManager = LinearLayoutManager(requireContext())

        orderAdapter = OrderAdapter(
            orders = orders,
            role = "seller",
            onActionClick = { order ->
                val next = nextStatusForSeller(order.status)
                if (next == null) {
                    Toast.makeText(
                        requireContext(),
                        "Order ini tidak bisa diubah lagi.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    updateOrderStatusToServer(order, next)
                }
            },
            onItemClick = { order ->
                // ✅ FIX: Pakai SellerOrderDetailFragment (read-only)
                val fragment = SellerOrderDetailFragment().apply {
                    arguments = Bundle().apply {
                        putInt("orderId", order.id)
                    }
                }

                // ✅ FIX: Pakai requireActivity() karena fragment_container ada di MainActivity
                requireActivity()
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.rvAdminOrders.adapter = orderAdapter
    }

    // ================== FETCH & UPDATE ORDERS (MVVM) ==================

    private fun fetchSellerOrders() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        // ✅ MVVM: Fetch via ViewModel
        orderViewModel.fetchSellerOrders(token)
    }

    private fun updateOrderStatusToServer(order: Order, nextStatusDb: String) {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        // ✅ MVVM: Update status via ViewModel
        orderViewModel.updateOrderStatus(
            token = token,
            orderId = order.id,
            nextStatus = nextStatusDb,
            onSuccess = {
                // Update local order object
                order.status = nextStatusDb
                orderAdapter.notifyDataSetChanged()

                // Refresh list from server
                fetchSellerOrders()
            }
        )
    }

    // ================== CLEANUP ==================

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}