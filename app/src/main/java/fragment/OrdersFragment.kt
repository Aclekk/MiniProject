package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.OrderAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.databinding.FragmentOrdersBinding
import com.example.miniproject.data.Order


class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(CartManager.orders) { order ->
            when (order.status) {
                "Belum Bayar" -> {
                    order.status = "Dikemas"
                    Toast.makeText(requireContext(), "Pesanan mulai dikemas!", Toast.LENGTH_SHORT).show()
                }
                "Dikemas" -> {
                    order.status = "Dikirim"
                    Toast.makeText(requireContext(), "Pesanan dikirim 🚚", Toast.LENGTH_SHORT).show()
                }
                "Dikirim" -> {
                    order.status = "Selesai"
                    Toast.makeText(requireContext(), "Pesanan selesai! 🎉", Toast.LENGTH_SHORT).show()
                }
                "Selesai" -> {
                    Toast.makeText(requireContext(), "Pesanan sudah selesai", Toast.LENGTH_SHORT).show()
                }
            }
            orderAdapter.notifyDataSetChanged()
        }

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = orderAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
