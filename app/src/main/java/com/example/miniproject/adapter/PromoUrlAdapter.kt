package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.data.model.PromoApi
import com.example.miniproject.databinding.ItemPromoBinding

class PromoUrlAdapter(
    private val promos: List<PromoApi>
) : RecyclerView.Adapter<PromoUrlAdapter.PromoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val binding = ItemPromoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PromoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        holder.bind(promos[position])
    }

    override fun getItemCount(): Int = promos.size

    inner class PromoViewHolder(
        private val binding: ItemPromoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(promo: PromoApi) {
            Glide.with(binding.root.context)
                .load(promo.image_url) // ✅ sesuai field di PromoApi
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivPromoImage)
        }
    }
}
