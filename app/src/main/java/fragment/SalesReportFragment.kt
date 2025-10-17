package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.SalesReportAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.databinding.FragmentSalesReportBinding
import java.text.SimpleDateFormat
import java.util.*

class SalesReportFragment : Fragment() {

    private var _binding: FragmentSalesReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calculateSalesReport()
        setupRecyclerView()
    }

    private fun calculateSalesReport() {
        // Ambil semua pesanan yang selesai
        val completedOrders = CartManager.orders.filter { it.status == "Selesai" }

        // Total penjualan keseluruhan
        val totalSales = completedOrders.sumOf { it.totalPrice }
        val totalOrders = completedOrders.size

        // Format tanggal hari ini
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())

        // Filter penjualan hari ini (simulasi - karena pakai dummy data)
        // Dalam real app, order punya timestamp
        val todaySales = completedOrders.sumOf { it.totalPrice } // Sementara ambil semua
        val todayOrders = completedOrders.size

        // Filter penjualan bulan ini (simulasi)
        val monthSales = completedOrders.sumOf { it.totalPrice }
        val monthOrders = completedOrders.size

        // Update UI
        binding.tvTotalSales.text = "Rp ${String.format("%,d", totalSales.toInt())}"
        binding.tvTotalOrders.text = "$totalOrders Pesanan"

        binding.tvTodaySales.text = "Rp ${String.format("%,d", todaySales.toInt())}"
        binding.tvTodayOrders.text = "$todayOrders Pesanan hari ini"

        binding.tvMonthSales.text = "Rp ${String.format("%,d", monthSales.toInt())}"
        binding.tvMonthOrders.text = "$monthOrders Pesanan bulan ini"
    }

    private fun setupRecyclerView() {
        val completedOrders = CartManager.orders.filter { it.status == "Selesai" }

        val adapter = SalesReportAdapter(completedOrders)
        binding.rvSalesReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSalesReport.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        calculateSalesReport()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}