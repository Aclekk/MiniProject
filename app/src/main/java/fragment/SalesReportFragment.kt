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
import com.example.miniproject.adapter.SalesReportAdapter
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.SalesReportData
import com.example.miniproject.databinding.FragmentSalesReportBinding
import kotlinx.coroutines.launch

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

        binding.rvSalesReport.layoutManager = LinearLayoutManager(requireContext())
        fetchSalesReport()
    }

    private fun fetchSalesReport() {
        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token hilang, login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getSalesReport(
                    token = "Bearer $token",
                    period = null,
                    startDate = null,
                    endDate = null
                )

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body?.success == true && body.data != null) {
                        bindReport(body.data)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            body?.message ?: "Gagal memuat laporan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal konek: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun bindReport(data: SalesReportData) {
        val summary = data.summary

        // =====================
        // SUMMARY HEADER
        // =====================
        binding.tvTotalSales.text =
            "Rp ${String.format("%,d", summary.totalRevenue.toInt())}"
        binding.tvTotalOrders.text =
            "${summary.totalOrders} Pesanan"

        binding.tvTodaySales.text =
            "Rp ${String.format("%,d", summary.todayRevenue.toInt())}"
        binding.tvTodayOrders.text =
            "${summary.todayOrders} Pesanan hari ini"

        binding.tvMonthSales.text =
            "Rp ${String.format("%,d", summary.monthRevenue.toInt())}"
        binding.tvMonthOrders.text =
            "${summary.monthOrders} Pesanan bulan ini"

        // =====================
        // LIST LAPORAN HARIAN
        // =====================
        binding.rvSalesReport.adapter = SalesReportAdapter(data.reports)
    }

    override fun onResume() {
        super.onResume()
        fetchSalesReport()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
