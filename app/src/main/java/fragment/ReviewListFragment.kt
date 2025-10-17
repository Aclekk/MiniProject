package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.ReviewAdapter
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.ProductDataSource
import com.example.miniproject.databinding.FragmentReviewListBinding

class ReviewListFragment : Fragment() {

    private var _binding: FragmentReviewListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil semua review dari CartManager
        val allReviews = CartManager.reviews.map { review ->
            val product = ProductDataSource.getAllProducts().find { it.id == review.productId }
            Triple(review, product?.name ?: "Produk Tidak Ditemukan", product?.imageResId)
        }

        if (allReviews.isEmpty()) {
            binding.tvEmptyReviews.visibility = View.VISIBLE
            binding.rvReviews.visibility = View.GONE
        } else {
            binding.tvEmptyReviews.visibility = View.GONE
            binding.rvReviews.visibility = View.VISIBLE

            val adapter = ReviewAdapter(allReviews)
            binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
            binding.rvReviews.adapter = adapter
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}