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
            // 2. Set Ikon Berdasarkan Nama Kategori
            val iconResId = when (category.categoryName) {
                "Pertanian" -> android.R.drawable.ic_menu_crop          // ikon daun
                "Pupuk"     -> android.R.drawable.ic_menu_gallery       // ikon pupuk (sementara)
                "Benih"     -> android.R.drawable.ic_menu_agenda        // ikon benih
                "Peralatan" -> android.R.drawable.ic_menu_manage        // ikon gear
                "Pestisida" -> android.R.drawable.ic_menu_info_details  // ikon info

                else        -> android.R.drawable.ic_menu_help          // cadangan
            }

            categoryIcon.setImageResource(iconResId) // Terapkan ikon ke ImageView

            // Opsional: Jika Anda memiliki TextView untuk product count (tvProductCount)
            // val productCount: TextView = itemView.findViewById(R.id.tvProductCount)
            // productCount.text = "${category.productCount} items"
        }
    }
}