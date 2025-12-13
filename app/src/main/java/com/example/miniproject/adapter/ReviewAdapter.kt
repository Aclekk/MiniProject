package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.R
import com.example.miniproject.data.Review
import com.example.miniproject.databinding.ItemReviewBinding

class ReviewAdapter(
    private val reviews: List<Triple<Review, String, Int?>>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Triple<Review, String, Int?>) {
            val (review, productName, imageResId) = item

            binding.tvProductName.text = productName
            binding.tvUserName.text = review.userName
            binding.tvReviewDate.text = review.createdAt
            binding.ratingBar.rating = review.rating
            binding.tvReviewComment.text = review.comment

            // Set gambar produk
            imageResId?.let {
                binding.ivProductImage.setImageResource(it)
            } ?: binding.ivProductImage.setImageResource(R.drawable.bg_card)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size
}