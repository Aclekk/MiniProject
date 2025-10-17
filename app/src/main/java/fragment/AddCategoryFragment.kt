package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.data.CategoryRepository
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

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnSaveCategory.setOnClickListener {
            saveCategory()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveCategory() {
        val categoryName = binding.etCategoryName.text.toString().trim()

        // Validasi input
        if (categoryName.isEmpty()) {
            binding.etCategoryName.error = "Nama kategori tidak boleh kosong"
            return
        }

        if (categoryName.length < 3) {
            binding.etCategoryName.error = "Nama kategori minimal 3 karakter"
            return
        }

        // Cek duplikat
        if (CategoryRepository.isCategoryNameExists(categoryName)) {
            binding.etCategoryName.error = "Kategori '$categoryName' sudah ada"
            Toast.makeText(
                requireContext(),
                "⚠️ Kategori '$categoryName' sudah ada!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Simpan kategori ke repository
        val success = CategoryRepository.addCategory(
            name = categoryName,
            iconResId = R.drawable.ic_category // Default icon
        )

        if (success) {
            Toast.makeText(
                requireContext(),
                "✅ Kategori '$categoryName' berhasil ditambahkan!",
                Toast.LENGTH_SHORT
            ).show()

            // Kembali ke CategoriesFragment
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(
                requireContext(),
                "❌ Gagal menambahkan kategori!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}