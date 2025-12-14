package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.data.model.SalesReportRow
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SalesReportAdapter : ListAdapter<SalesReportRow, SalesReportAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvReportDate)
        private val tvOrders: TextView = itemView.findViewById(R.id.tvTotalOrders)
        private val tvRevenue: TextView = itemView.findViewById(R.id.tvTotalRevenue)
        private val tvItems: TextView = itemView.findViewById(R.id.tvTotalItems)

        fun bind(report: SalesReportRow) {
            // Format tanggal
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            try {
                val date = dateFormat.parse(report.reportDate)
                tvDate.text = date?.let { displayFormat.format(it) } ?: report.reportDate
            } catch (e: Exception) {
                tvDate.text = report.reportDate
            }

            // Format angka
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            tvOrders.text = "${report.totalOrders} pesanan"
            tvRevenue.text = currencyFormat.format(report.totalRevenue)
            tvItems.text = "${report.totalItemsSold} item"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SalesReportRow>() {
        override fun areItemsTheSame(oldItem: SalesReportRow, newItem: SalesReportRow): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SalesReportRow, newItem: SalesReportRow): Boolean {
            return oldItem == newItem
        }
    }
}