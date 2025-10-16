package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.miniproject.adapter.CategoryAdapter
import com.example.miniproject.databinding.FragmentCategoriesBinding
import com.example.miniproject.model.DummyDataRepository

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories = DummyDataRepository.categories,
            onCategoryClick = { category ->
                // Navigate to products by category
                val productsFragment = ProductsFragment().apply {
                    arguments = Bundle().apply {
                        putString("categoryId", category.categoryId)
                        putString("categoryName", category.categoryName)
                    }
                }

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(com.example.miniproject.R.id.fragment_container, productsFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
        }
    }

    private fun loadCategories() {
        val categories = DummyDataRepository.getAllCategories()

        if (categories.isEmpty()) {
            binding.tvEmptyCategories.visibility = View.VISIBLE
            binding.rvCategories.visibility = View.GONE
        } else {
            binding.tvEmptyCategories.visibility = View.GONE
            binding.rvCategories.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}