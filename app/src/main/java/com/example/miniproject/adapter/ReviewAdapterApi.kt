package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.miniproject.R
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.ReviewApi
import com.example.miniproject.databinding.ItemReviewBinding

class ReviewAdapterApi(
    private val reviews: List<ReviewApi>
) : RecyclerView.Adapter<ReviewAdapterApi.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(r: ReviewApi) {
            binding.tvProductName.text = r.productName ?: "Produk"
            binding.tvUserName.text = r.userName ?: "Anda"
            binding.tvReviewDate.text = r.createdAt ?: "-"
            binding.ratingBar.rating = (r.rating.coerceIn(1, 5)).toFloat()
            binding.tvReviewComment.text = r.comment ?: ""

            val url = ApiClient.getImageUrl(r.productImage)
            if (url.isNotEmpty()) {
                Glide.with(binding.ivProductImage.context)
                    .load(url)
                    .placeholder(R.drawable.bg_card)
                    .error(R.drawable.bg_card)
                    .into(binding.ivProductImage)
            } else {
                binding.ivProductImage.setImageResource(R.drawable.bg_card)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size
}
