package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Import ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.model.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
        holder.itemView.setOnClickListener { onItemClick(category) }
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon) // Inisialisasi ImageView baru

        fun bind(category: Category) {
            // 1. Set Nama Kategori
            categoryName.text = category.categoryName

            // 2. Set Ikon Berdasarkan Nama Kategori
            val iconResId = when (category.categoryName) {
                "Pertanian" -> R.drawable.pertanian
                "Pupuk"     -> R.drawable.pupukk     // Menggunakan pupukk.png
                "Benih"     -> R.drawable.benihh     // Menggunakan benihh.png
                "Peralatan" -> R.drawable.peralatan
                "Pestisida" -> R.drawable.pest

                // Tambahkan kategori lain jika ada
                else        -> R.drawable.ic_default_category // Ikon cadangan
            }

            categoryIcon.setImageResource(iconResId) // Terapkan ikon ke ImageView

            // Opsional: Jika Anda memiliki TextView untuk product count (tvProductCount)
            // val productCount: TextView = itemView.findViewById(R.id.tvProductCount)
            // productCount.text = "${category.productCount} items"
        }
    }
}