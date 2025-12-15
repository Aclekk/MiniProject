package com.example.miniproject.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.OrderAdapter
import com.example.miniproject.data.Order
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.mapper.toOrderModel
import com.example.miniproject.databinding.FragmentAdminOrderListBinding
import com.example.miniproject.firebase.MyFirebaseMessagingService
import com.example.miniproject.util.nextStatusForSeller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminOrderListFragment : Fragment() {

    private var _binding: FragmentAdminOrderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderAdapter: OrderAdapter
    private val orders: MutableList<Order> = mutableListOf()

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
        setupRecyclerView()
        fetchSellerOrders()
    }

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

    private fun fetchSellerOrders() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.apiService.getSellerOrders(token = "Bearer $token")
                withContext(Dispatchers.Main) {
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        if (body?.success == true && body.data != null) {
                            val mapped = body.data.map { it.toOrderModel() }
                            orderAdapter.replaceAll(mapped)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                body?.message ?: "Gagal memuat pesanan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "HTTP ${resp.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateOrderStatusToServer(order: Order, nextStatusDb: String) {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val payload = mapOf(
                    "order_id" to order.id.toString(),
                    "status" to nextStatusDb
                )

                val resp = ApiClient.apiService.updateOrderStatus(
                    token = "Bearer $token",
                    body = payload
                )

                withContext(Dispatchers.Main) {
                    if (resp.isSuccessful && resp.body()?.success == true) {
                        order.status = nextStatusDb
                        orderAdapter.notifyDataSetChanged()
                        fetchSellerOrders()
                        Toast.makeText(
                            requireContext(),
                            "Status berhasil diupdate!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Gagal update status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}