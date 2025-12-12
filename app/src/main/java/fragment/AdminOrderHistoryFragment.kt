package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.OrderAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.FragmentCompletedOrdersBinding

class AdminOrderHistoryFragment : Fragment() {

    private var _binding: FragmentCompletedOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderAdapter
    private val completedOrders: MutableList<Order> = mutableListOf()

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
        loadData()
    }

    private fun setupRecyclerView() {
        binding.rvCompletedOrders.layoutManager = LinearLayoutManager(requireContext())

        // IMPORTANT: jangan pakai named argument onNextStatus kalau constructor adapter kamu beda
        // Panggil positional saja biar aman.
        adapter = OrderAdapter(completedOrders, null)

        binding.rvCompletedOrders.adapter = adapter
    }

    private fun loadData() {
        completedOrders.clear()
        completedOrders.addAll(
            CartManager.orders.filter {
                it.status.equals("Selesai", ignoreCase = true) ||
                        it.status.equals("completed", ignoreCase = true)
            }
        )
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
