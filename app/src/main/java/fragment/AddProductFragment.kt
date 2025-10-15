package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.utils.ProductStorage
import com.example.miniproject.databinding.FragmentAddProductBinding
import com.example.miniproject.model.Product

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val dummyCategories = listOf("Pertanian", "Pupuk", "Benih", "Peralatan", "Pestisida")

    // Jika edit, kita ambil dari argument
    private var editingProduct: Product? = null

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

        setupCategorySpinner()

        // Cek apakah fragment ini digunakan untuk edit produk
        editingProduct = arguments?.getParcelable("product_data")
        if (editingProduct != null) {
            binding.etProductName.setText(editingProduct!!.name)
            binding.etProductPrice.setText(editingProduct!!.price.toString())
            binding.etProductDescription.setText(editingProduct!!.description)
            binding.etProductStock.setText(editingProduct!!.stock.toString())

            val pos = dummyCategories.indexOf(editingProduct!!.categoryName)
            if (pos >= 0) binding.spinnerCategory.setSelection(pos)

            binding.btnSaveProduct.text = "Update Produk"
        }

        binding.btnSaveProduct.setOnClickListener { saveProduct() }
        binding.btnCancel.setOnClickListener { goBackToProducts() }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dummyCategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()
        val selectedCategory = binding.spinnerCategory.selectedItem.toString()

        if (!validateInput(name, priceStr, stockStr)) return

        val price = priceStr.toDouble()
        val stock = stockStr.toInt()
        val context = requireContext()
        val list = ProductStorage.loadProducts(context)

        if (editingProduct != null) {
            // EDIT
            val index = list.indexOfFirst { it.id == editingProduct!!.id }
            if (index >= 0) {
                list[index] = editingProduct!!.copy(
                    name = name,
                    price = price,
                    description = description,
                    stock = stock,
                    categoryName = selectedCategory
                )
                Toast.makeText(context, "Produk berhasil diperbarui ✅", Toast.LENGTH_SHORT).show()
            }
        } else {
            // TAMBAH BARU
            val newProduct = Product(
                id = System.currentTimeMillis().toInt(),
                name = name,
                price = price,
                description = description,
                imageUrl = null,
                imageResId = R.drawable.bg_card,
                categoryId = 1,
                stock = stock,
                categoryName = selectedCategory,
                createdAt = "2025-01-01"
            )
            list.add(newProduct)
            Toast.makeText(context, "Produk baru disimpan ✅", Toast.LENGTH_SHORT).show()
        }

        ProductStorage.saveProducts(context, list)
        goBackToProducts()
    }

    private fun validateInput(name: String, price: String, stock: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.etProductName.error = "Nama wajib diisi"; false
            }
            price.isEmpty() -> {
                binding.etProductPrice.error = "Harga wajib diisi"; false
            }
            stock.isEmpty() -> {
                binding.etProductStock.error = "Stok wajib diisi"; false
            }
            else -> true
        }
    }

    private fun goBackToProducts() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProductsFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
