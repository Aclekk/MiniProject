package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.databinding.FragmentAddCategoryBinding

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

        // DATA DUMMY - Simpan ke list global atau SharedPreferences
        // Untuk sekarang, cukup tampilkan Toast dan kembali
        Toast.makeText(requireContext(), "Category '$categoryName' added successfully", Toast.LENGTH_SHORT).show()

        // Clear input
        binding.etCategoryName.text?.clear()

        // Kembali ke CategoriesFragment
        navigateToCategoriesFragment()
    }

    private fun navigateToCategoriesFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CategoriesFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}