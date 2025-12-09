package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.adapter.OrderAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Order
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentAdminOrderListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminOrderListFragment : Fragment() {

    private var _binding: FragmentAdminOrderListBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderAdapter: OrderAdapter
    private val orders: MutableList<Order> = mutableListOf()

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

        // Ambil dari CartManager (logic lama)
        orders.clear()
        orders.addAll(CartManager.orders)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // ⚠️ Pastikan id RecyclerView di fragment_admin_order_list.xml sama.
        // Kalau beda, ganti "rvAdminOrders" di sini dengan id milikmu.
        binding.rvAdminOrders.layoutManager = LinearLayoutManager(requireContext())

        orderAdapter = OrderAdapter(orders) { order ->
            val nextStatus = getNextStatusFor(order.status)

            if (nextStatus == null) {
                Toast.makeText(requireContext(), "Status ini tidak bisa diubah.", Toast.LENGTH_SHORT).show()
            } else {
                updateOrderStatusToServer(order, nextStatus)
            }
        }

        binding.rvAdminOrders.adapter = orderAdapter
    }

    /**
     * Tentukan status berikutnya untuk tombol seller.
     * Kamu bisa atur ulang kalau mau flow lain.
     */
    private fun getNextStatusFor(currentStatus: String): String? {
        val s = currentStatus.lowercase()
        return when (s) {
            "pending", "menunggu konfirmasi" -> "Dikemas"   // 🔥 konfirmasi pesanan
            "dikemas"                        -> "Dikirim"
            "dikirim"                        -> "Selesai"
            else                             -> null
        }
    }


    /**
     * Panggil API orders/update_status.php
     */
    private fun updateOrderStatusToServer(order: Order, nextStatus: String) {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val body: Map<String, String> = mapOf(
                    "order_id" to order.id.toString(),
                    "status"   to nextStatus
                )

                val response = ApiClient.apiService.updateOrderStatus(
                    "Bearer $token",
                    body
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val resBody = response.body()
                        if (resBody?.success == true) {
                            // ✅ Berhasil
                            order.status = nextStatus
                            orderAdapter.notifyDataSetChanged()
                            Toast.makeText(
                                requireContext(),
                                "Status diperbarui menjadi $nextStatus",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val msg = resBody?.message ?: "Update status gagal (success=false)"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // ✅ Tampilkan pesan error asli dari PHP
                        val errorText = response.errorBody()?.string()
                        Toast.makeText(
                            requireContext(),
                            "HTTP ${response.code()}: ${errorText ?: "Gagal update status"}",
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
