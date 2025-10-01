package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.api.ApiClient
import com.example.miniproject.databinding.FragmentAddCategoryBinding
import com.example.miniproject.model.CategoryRequest
import com.example.miniproject.model.CategoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddCategoryFragment : Fragment() {

    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveCategory.setOnClickListener { saveCategory() }
        binding.btnCancel.setOnClickListener { navigateToCategoriesFragment() }
    }

    private fun saveCategory() {
        val categoryName = binding.etCategoryName.text.toString().trim()

        if (categoryName.isEmpty()) {
            binding.etCategoryName.error = "Category name cannot be empty"
            return
        }

        showLoading(true)

        val categoryRequest = CategoryRequest(categoryName = categoryName)
        ApiClient.apiService.createCategory(categoryRequest).enqueue(object : Callback<CategoryResponse> {
            override fun onResponse(call: Call<CategoryResponse>, response: Response<CategoryResponse>) {
                showLoading(false)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Category added successfully", Toast.LENGTH_SHORT).show()
                    navigateToCategoriesFragment()
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to add category"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CategoryResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveCategory.isEnabled = !isLoading
        binding.btnCancel.isEnabled = !isLoading
    }

    private fun navigateToCategoriesFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CategoriesFragment()) // Pastikan CategoriesFragment ada
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
