package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.ReviewManagementAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.databinding.FragmentReviewManagementBinding

class ReviewManagementFragment : Fragment() {
    private var _binding: FragmentReviewManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewAdapter: ReviewManagementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        updateReviewStats()
    }

    private fun setupRecyclerView() {
        val reviews = CartManager.getAllReviews()

        if (reviews.isEmpty()) {
            binding.tvEmptyReview.visibility = View.VISIBLE
            binding.rvReviews.visibility = View.GONE
        } else {
            binding.tvEmptyReview.visibility = View.GONE
            binding.rvReviews.visibility = View.VISIBLE

            reviewAdapter = ReviewManagementAdapter(reviews)
            binding.rvReviews.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = reviewAdapter
            }
        }
    }

    private fun updateReviewStats() {
        val reviews = CartManager.getAllReviews()
        val totalReviews = reviews.size
        val averageRating = if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average()
        } else {
            0.0
        }

        // Hitung distribusi rating
        val rating5 = reviews.count { it.rating >= 4.5f }
        val rating4 = reviews.count { it.rating >= 3.5f && it.rating < 4.5f }
        val rating3 = reviews.count { it.rating >= 2.5f && it.rating < 3.5f }
        val rating2 = reviews.count { it.rating >= 1.5f && it.rating < 2.5f }
        val rating1 = reviews.count { it.rating < 1.5f }

        binding.tvReviewStats.text = """
            ⭐ Total Ulasan: $totalReviews
            📊 Rating Rata-rata: ${"%.1f".format(averageRating)} / 5.0
            
            ⭐⭐⭐⭐⭐ : $rating5
            ⭐⭐⭐⭐ : $rating4
            ⭐⭐⭐ : $rating3
            ⭐⭐ : $rating2
            ⭐ : $rating1
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}