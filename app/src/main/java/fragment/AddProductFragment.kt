package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.databinding.FragmentAddProductBinding

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    // DUMMY CATEGORIES
    private val dummyCategories = listOf(
        "Pertanian",
        "Pupuk",
        "Benih",
        "Peralatan",
        "Pestisida"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup spinner dengan dummy categories
        setupCategorySpinner()

        // Setup click listeners
        binding.btnSaveProduct.setOnClickListener { saveProduct() }
        binding.btnCancel.setOnClickListener { goBackToProducts() }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dummyCategories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()
        val selectedCategory = binding.spinnerCategory.selectedItem.toString()

        // Validasi input
        if (!validateInput(name, priceStr, stockStr)) return

        val price = priceStr.toDouble()
        val stock = stockStr.toInt()

        // DUMMY SAVE - Hanya tampil Toast
        Toast.makeText(
            requireContext(),
            "Product '$name' added successfully!\nCategory: $selectedCategory\nPrice: Rp $price\nStock: $stock",
            Toast.LENGTH_LONG
        ).show()

        // Clear input
        clearInput()

        // Kembali ke ProductsFragment
        goBackToProducts()
    }

    private fun validateInput(name: String, price: String, stock: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.etProductName.error = "Name required"
                false
            }
            price.isEmpty() -> {
                binding.etProductPrice.error = "Price required"
                false
            }
            stock.isEmpty() -> {
                binding.etProductStock.error = "Stock required"
                false
            }
            else -> {
                try {
                    price.toDouble()
                    stock.toInt()
                    true
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        requireContext(),
                        "Invalid price or stock format",
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                }
            }
        }
    }

    private fun clearInput() {
        binding.etProductName.text?.clear()
        binding.etProductPrice.text?.clear()
        binding.etProductDescription.text?.clear()
        binding.etProductImageUrl.text?.clear()
        binding.etProductStock.text?.clear()
    }

    private fun goBackToProducts() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProductsFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}