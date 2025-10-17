package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.R
import com.example.miniproject.adapter.OrderHistoryAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.FragmentActiveOrdersBinding

class ActiveOrdersFragment : Fragment() {

    private var _binding: FragmentActiveOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderHistoryAdapter

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

        val activeOrders: MutableList<Order> = CartManager.orders
            .filter { it.status != "Selesai" }
            .toMutableList()

        adapter = OrderHistoryAdapter(
            orders = activeOrders,
            onOrderClick = { order ->
                val detailFragment = OrderDetailFragment().apply {
                    arguments = Bundle().apply {
                        putInt("orderId", order.id)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            isActiveTab = true // 🔥 HAPUS parameter isAdminView
        )

        binding.rvActiveOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveOrders.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val updatedOrders = CartManager.orders.filter { it.status != "Selesai" }
        adapter.orders.clear()
        adapter.orders.addAll(updatedOrders)
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}