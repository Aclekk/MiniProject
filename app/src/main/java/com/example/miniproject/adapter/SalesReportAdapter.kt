package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.data.model.SalesReportRow
import com.example.miniproject.databinding.ItemSalesReportBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SalesReportAdapter(
    private val reports: List<SalesReportRow>
) : RecyclerView.Adapter<SalesReportAdapter.SalesViewHolder>() {

    inner class SalesViewHolder(val binding: ItemSalesReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val binding = ItemSalesReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SalesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val row = reports[position]

        holder.binding.tvOrderId.text = "Report #${row.id}"
        holder.binding.tvOrderDate.text = formatDate(row.reportDate)

        holder.binding.tvOrderItems.text = "Items terjual: ${row.totalItemsSold}"
        holder.binding.tvOrderTotal.text = "Rp ${String.format("%,d", row.totalRevenue.toInt())}"
        holder.binding.tvPaymentMethod.text = "Total pesanan: ${row.totalOrders}"
    }

    override fun getItemCount() = reports.size

    private fun formatDate(date: String): String {
        return try {
            // backend: YYYY-MM-DD
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val d = input.parse(date)
            if (d != null) output.format(d) else date
        } catch (_: Exception) {
            date
        }
    }
}
