package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.data.CategoryRepository
import com.example.miniproject.databinding.FragmentEditCategoryBinding

class EditCategoryFragment : Fragment() {

    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!

    private var categoryId: Int = -1
    private var oldCategoryName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari arguments
        categoryId = arguments?.getInt("category_id", -1) ?: -1
        oldCategoryName = arguments?.getString("category_name", "") ?: ""

        if (categoryId == -1) {
            Toast.makeText(requireContext(), "❌ Error: Category tidak ditemukan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // Set data ke form
        binding.etCategoryName.setText(oldCategoryName)

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnUpdateCategory.setOnClickListener {
            updateCategory()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateCategory() {
        val newCategoryName = binding.etCategoryName.text.toString().trim()

        // Validasi input
        if (newCategoryName.isEmpty()) {
            binding.etCategoryName.error = "Nama kategori tidak boleh kosong"
            return
        }

        if (newCategoryName.length < 3) {
            binding.etCategoryName.error = "Nama kategori minimal 3 karakter"
            return
        }

        // Cek duplikat (kecuali nama lama)
        if (newCategoryName != oldCategoryName && CategoryRepository.isCategoryNameExists(newCategoryName)) {
            binding.etCategoryName.error = "Kategori '$newCategoryName' sudah ada"
            Toast.makeText(
                requireContext(),
                "⚠️ Kategori '$newCategoryName' sudah ada!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Update kategori
        val success = CategoryRepository.updateCategory(categoryId, newCategoryName)

        if (success) {
            Toast.makeText(
                requireContext(),
                "✅ Kategori berhasil diupdate!",
                Toast.LENGTH_SHORT
            ).show()
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(
                requireContext(),
                "❌ Gagal mengupdate kategori!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}