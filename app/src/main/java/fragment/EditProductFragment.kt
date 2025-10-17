package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.data.ProductDataSource
import com.example.miniproject.databinding.FragmentEditProductBinding
import com.example.miniproject.model.Product

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!
    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product = arguments?.getParcelable("product")

        if (product != null) {
            populateForm(product!!)
            setupClickListeners()
        } else {
            Toast.makeText(context, "Error: Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun populateForm(product: Product) {
        binding.etProductName.setText(product.name)
        binding.etProductPrice.setText(product.price.toInt().toString())
        binding.etProductDescription.setText(product.description)
        binding.etProductStock.setText(product.stock.toString())
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveProduct()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val description = binding.etProductDescription.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()

        if (name.isEmpty()) {
            binding.etProductName.error = "Nama produk harus diisi"
            return
        }
        if (priceStr.isEmpty()) {
            binding.etProductPrice.error = "Harga harus diisi"
            return
        }
        if (stockStr.isEmpty()) {
            binding.etProductStock.error = "Stok harus diisi"
            return
        }

        val updatedProduct = product!!.copy(
            name = name,
            price = priceStr.toDouble(),
            description = description,
            stock = stockStr.toInt()
        )

        // Update ke ProductDataSource
        ProductDataSource.updateProduct(updatedProduct)

        Toast.makeText(context, "✅ Produk berhasil diupdate!", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}