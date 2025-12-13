package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.ReviewAdapterApi
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.ReviewApi
import com.example.miniproject.databinding.FragmentReviewListBinding
import kotlinx.coroutines.launch

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

        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())

        loadReviews()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadReviews() {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val role = prefs.getString("role", "buyer") // pastikan kamu simpan role saat login

        if (token.isNullOrEmpty()) {
            showEmpty()
            return
        }

        lifecycleScope.launch {
            try {
                val authHeader = "Bearer $token"

                val res = if (role == "seller" || role == "admin") {
                    ApiClient.apiService.getSellerReviews(authHeader)
                } else {
                    ApiClient.apiService.getBuyerReviews(authHeader)
                }

                if (res.isSuccessful && res.body()?.success == true) {
                    val list = res.body()?.data.orEmpty()
                    if (list.isEmpty()) showEmpty() else showList(list)
                } else {
                    showEmpty()
                }
            } catch (_: Exception) {
                showEmpty()
            }
        }
    }

    private fun showEmpty() {
        binding.tvEmptyReviews.visibility = View.VISIBLE
        binding.rvReviews.visibility = View.GONE
    }

    private fun showList(reviews: List<ReviewApi>) {
        binding.tvEmptyReviews.visibility = View.GONE
        binding.rvReviews.visibility = View.VISIBLE
        binding.rvReviews.adapter = ReviewAdapterApi(reviews)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
