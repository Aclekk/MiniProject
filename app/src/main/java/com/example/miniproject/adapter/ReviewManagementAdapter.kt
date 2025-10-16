package com.example.miniproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.miniproject.databinding.ItemReviewManagementBinding
import com.example.miniproject.model.Review
import java.text.SimpleDateFormat
import java.util.*

class ReviewManagementAdapter(
    private val reviews: List<Review>
) : RecyclerView.Adapter<ReviewManagementAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemReviewManagementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

            binding.tvUserName.text = review.userName
            binding.tvProductName.text = review.productName
            binding.ratingBar.rating = review.rating
            binding.tvRating.text = review.rating.toString()
            binding.tvComment.text = review.comment
            binding.tvReviewDate.text = dateFormat.format(Date(review.reviewDate))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewManagementBinding.inflate(
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