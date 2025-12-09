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
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.FragmentActiveOrdersBinding

/**
 * Fragment untuk SELLER – menampilkan pesanan aktif dan
 * menyediakan tombol untuk mengubah status pesanan.
 *
 * Semua masih LOCAL menggunakan CartManager.orders (belum ke database).
 */
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
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Ambil semua order yang BELUM selesai
        val activeOrders: List<Order> = CartManager.orders.filter { it.status != "Selesai" }

        Log.d("AdminOrderList", "Active orders: ${activeOrders.size}")

        adapter = OrderAdapter(
            orders = activeOrders,
            onNextStatus = { order ->
                val oldStatus = order.status

                // Flow sederhana: Dikemas -> Dikirim -> (optional) Selesai
                order.status = when (order.status) {
                    "Dikemas" -> "Dikirim"
                    "Dikirim" -> "Selesai"
                    else -> order.status
                }

                Toast.makeText(
                    requireContext(),
                    "Status diubah: $oldStatus → ${order.status}",
                    Toast.LENGTH_SHORT
                ).show()

                // Refresh tampilan list
                refreshList()
            }
        )

        binding.rvActiveOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveOrders.adapter = adapter
    }

    private fun refreshList() {
        val updatedOrders = CartManager.orders.filter { it.status != "Selesai" }
        adapter.orders = updatedOrders
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
