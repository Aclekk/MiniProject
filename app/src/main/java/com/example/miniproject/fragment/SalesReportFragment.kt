package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.miniproject.databinding.FragmentSalesReportBinding
import com.example.miniproject.data.CartManager
import java.text.NumberFormat
import java.util.*

class SalesReportFragment : Fragment() {

    private var _binding: FragmentSalesReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showReport()
    }

    private fun showReport() {
        val orders = CartManager.getOrders()

        if (orders.isEmpty()) {
            binding.tvSummary.text = "Belum ada data penjualan 😅"
            binding.tvTotalOrders.text = "-"
            binding.tvTotalRevenue.text = "-"
            binding.tvAvgRevenue.text = "-"
            return
        }

        // 🔹 Total pesanan
        val totalOrders = orders.size

        // 🔹 Total pendapatan
        val totalRevenue = orders.sumOf { it.totalPrice }

        // 🔹 Rata-rata pendapatan
        val avgRevenue =
            if (totalOrders > 0) totalRevenue / totalOrders else 0.0

        // 🔹 Format angka ke Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // 🔹 Tampilkan ke UI
        binding.tvSummary.text = "Laporan Penjualan"
        binding.tvTotalOrders.text = "Total Pesanan: $totalOrders"
        binding.tvTotalRevenue.text =
            "Total Pendapatan: ${formatRupiah.format(totalRevenue)}"
        binding.tvAvgRevenue.text =
            "Rata-rata Pendapatan: ${formatRupiah.format(avgRevenue)}"

        // 🔹 (Opsional) bisa tambahkan list produk paling sering dipesan
        val allItems = orders.flatMap { it.items ?: emptyList() }
        val bestSeller = allItems
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }

        binding.tvBestSeller.text = if (bestSeller != null) {
            "Produk Terlaris: ${bestSeller.key} (${bestSeller.value}x terjual)"
        } else {
            "Belum ada data produk terlaris"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
