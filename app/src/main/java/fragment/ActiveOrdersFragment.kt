package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.OrderHistoryAdapter
import com.example.miniproject.data.Order
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.mapper.toOrderModel
import com.example.miniproject.databinding.FragmentActiveOrdersBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActiveOrdersFragment : Fragment() {

    private var _binding: FragmentActiveOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderHistoryAdapter
    private val orders: MutableList<Order> = mutableListOf()

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

    override fun onResume() {
        super.onResume()
        // biar abis notif/status update, pas balik tab langsung refresh
        fetchBuyerOrders()
    }

    private fun fetchBuyerOrders() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan, login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.apiService.getBuyerOrders(
                    token = "Bearer $token",
                    status = null
                )

                withContext(Dispatchers.Main) {
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        if (body?.success == true && body.data != null) {
                            orders.clear()
                            orders.addAll(body.data.map { it.toOrderModel() })
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                body?.message ?: "Gagal memuat pesanan buyer",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val err = resp.errorBody()?.string()
                        Toast.makeText(
                            requireContext(),
                            "HTTP ${resp.code()}: ${err ?: "error"}",
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
