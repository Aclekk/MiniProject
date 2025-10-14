package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.model.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)

        fun bind(category: Category) {
            tvCategoryName.text = category.categoryName

            // Set different icons for different categories
            val iconRes = when {
                category.categoryName.contains("Bajak", ignoreCase = true) -> android.R.drawable.ic_menu_manage
                category.categoryName.contains("Panen", ignoreCase = true) -> android.R.drawable.ic_menu_crop
                category.categoryName.contains("Siram", ignoreCase = true) -> android.R.drawable.ic_dialog_info
                category.categoryName.contains("Pupuk", ignoreCase = true) -> android.R.drawable.ic_menu_add
                category.categoryName.contains("Benih", ignoreCase = true) -> android.R.drawable.ic_menu_compass
                else -> android.R.drawable.ic_menu_sort_by_size
            }
            ivCategoryIcon.setImageResource(iconRes)

            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}