package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.data.Review
import com.example.miniproject.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            binding.tvUserName.text = review.userName
            binding.rbUserRating.rating = review.rating
            binding.tvReviewComment.text = review.comment

            // Format date
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                val date = inputFormat.parse(review.createdAt)
                binding.tvReviewDate.text = date?.let { outputFormat.format(it) } ?: review.createdAt
            } catch (e: Exception) {
                binding.tvReviewDate.text = review.createdAt
            }

            // User initial for avatar
            val initial = review.userName.firstOrNull()?.uppercase() ?: "?"
            binding.tvUserInitial.text = initial
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

    override fun getItemCount(): Int = reviews.size
}