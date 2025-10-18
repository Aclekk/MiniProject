package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.model.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val userRole: String = "user", // ✅ Default user
    private val onItemClick: (Category) -> Unit,
    private val onEditClick: ((Category) -> Unit)? = null, // ✅ Callback edit
    private val onDeleteClick: ((Category) -> Unit)? = null // ✅ Callback delete
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val categoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)

        // Tombol admin (pastikan ada di layout item_category.xml)
        private val btnEdit: Button? = itemView.findViewById(R.id.btnEditCategory)
        private val btnDelete: Button? = itemView.findViewById(R.id.btnDeleteCategory)

        fun bind(category: Category) {
            // ✅ Set nama kategori
            categoryName.text = category.categoryName

            // ✅ Pakai ikon langsung dari repository
            categoryIcon.setImageResource(category.icon ?: R.drawable.ic_category)


            // ✅ Tampilkan tombol edit/delete hanya untuk admin
            if (userRole == "admin") {
                btnEdit?.visibility = View.VISIBLE
                btnDelete?.visibility = View.VISIBLE

                btnEdit?.setOnClickListener { onEditClick?.invoke(category) }
                btnDelete?.setOnClickListener { onDeleteClick?.invoke(category) }
            } else {
                btnEdit?.visibility = View.GONE
                btnDelete?.visibility = View.GONE
            }

            // ✅ Klik item untuk lihat produk
            itemView.setOnClickListener { onItemClick(category) }
        }
    }
}
