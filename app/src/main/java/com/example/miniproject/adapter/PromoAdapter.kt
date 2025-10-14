package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.databinding.ItemPromoBinding
import com.example.miniproject.model.Promo

class PromoAdapter(private val promos: List<Promo>) :
    RecyclerView.Adapter<PromoAdapter.PromoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val binding = ItemPromoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PromoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        holder.bind(promos[position])
    }

    override fun getItemCount(): Int = promos.size

    inner class PromoViewHolder(private val binding: ItemPromoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(promo: Promo) {
            binding.tvPromoTitle.text = promo.title
            binding.tvPromoDescription.text = promo.description
            binding.ivPromoImage.setImageResource(promo.imageResId)
        }
    }
}
