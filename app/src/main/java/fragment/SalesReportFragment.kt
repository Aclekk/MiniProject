package com.example.miniproject.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.SalesReportAdapter
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.databinding.FragmentSalesReportBinding
import com.example.miniproject.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class SalesReportFragment : Fragment() {

    private var _binding: FragmentSalesReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SalesReportAdapter
    private lateinit var sessionManager: SessionManager

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

        sessionManager = SessionManager(requireContext())
        setupRecyclerView()
        loadSalesReport("all")
    }

    private fun setupRecyclerView() {
        adapter = SalesReportAdapter()
        binding.rvSalesReport.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SalesReportFragment.adapter
        }
    }

    private fun loadSalesReport(period: String) {
        val token = sessionManager.getToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan, login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SalesReport", "Loading report with period: $period")
        Log.d("SalesReport", "Token: ${token.take(20)}...")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getSalesReport(
                    token = "Bearer $token",
                    period = period,
                    startDate = null,
                    endDate = null
                )

                Log.d("SalesReport", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("SalesReport", "Response body: $responseBody")

                    if (responseBody?.success == true) {
                        val salesData = responseBody.data
                        if (salesData != null) {
                            updateUI(salesData)
                            Log.d("SalesReport", "Data loaded: ${salesData.reports.size} reports")
                        } else {
                            Toast.makeText(requireContext(), "Data tidak tersedia", Toast.LENGTH_SHORT).show()
                            Log.e("SalesReport", "Data is null")
                        }
                    } else {
                        val message = responseBody?.message ?: "Gagal memuat laporan"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        Log.e("SalesReport", "API error: $message")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("SalesReport", "HTTP error ${response.code()}: $errorBody")
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("SalesReport", "Exception: ${e.message}", e)
            }
        }
    }

    private fun updateUI(salesData: com.example.miniproject.data.model.SalesReportData) {
        val summary = salesData.summary
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // Update summary cards - hanya yang ADA di layout
        binding.tvTotalSales.text = currencyFormat.format(summary.totalRevenue)
        binding.tvTotalOrders.text = "${summary.totalOrders} Pesanan"

        binding.tvTodaySales.text = currencyFormat.format(summary.todayRevenue)
        binding.tvTodayOrders.text = "${summary.todayOrders} Pesanan"

        binding.tvMonthSales.text = currencyFormat.format(summary.monthRevenue)
        binding.tvMonthOrders.text = "${summary.monthOrders} Pesanan"

        // Update list
        adapter.submitList(salesData.reports)

        // Show/hide RecyclerView based on data
        if (salesData.reports.isEmpty()) {
            binding.rvSalesReport.visibility = View.GONE
            Toast.makeText(requireContext(), "Belum ada data penjualan", Toast.LENGTH_SHORT).show()
        } else {
            binding.rvSalesReport.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadSalesReport("all")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}