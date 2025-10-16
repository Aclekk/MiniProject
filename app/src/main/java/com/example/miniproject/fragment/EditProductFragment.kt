package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.miniproject.databinding.FragmentEditProductBinding

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private var currentProductId: String? = null
    private var currentCategoryId: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            currentProductId = it.getString("EXTRA_ID")
            binding.etNamaProduk.setText(it.getString("EXTRA_NAMA"))
            binding.etHargaProduk.setText(it.getString("EXTRA_HARGA"))
            binding.etDeskripsiProduk.setText(it.getString("EXTRA_DESKRIPSI"))
            binding.etStokProduk.setText(it.getString("EXTRA_STOK"))
        }
        binding.btnSimpanPerubahan.setOnClickListener { updateProductData() }
    }

    private fun updateProductData() {
        val updatedNama = binding.etNamaProduk.text.toString().trim()
        val updatedHargaStr = binding.etHargaProduk.text.toString().trim()
        val updatedDeskripsi = binding.etDeskripsiProduk.text.toString().trim()
        val updatedStokStr = binding.etStokProduk.text.toString().trim()

        if (updatedNama.isEmpty() || updatedHargaStr.isEmpty() || updatedDeskripsi.isEmpty() || updatedStokStr.isEmpty()) {
            Toast.makeText(requireContext(), "Semua kolom harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val productIdInt = currentProductId?.toIntOrNull()
        if (productIdInt == null) {
            Toast.makeText(requireContext(), "Error: ID Produk tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val productRequest = ProductRequest(
            name = updatedNama,
            price = try { updatedHargaStr.toDouble() } catch (e: Exception) { 0.0 },
            description = updatedDeskripsi,
            imageUrl = "placeholder_image.jpg",
            categoryId = currentCategoryId,
            stock = try { updatedStokStr.toInt() } catch (e: Exception) { 0 }
        )

        // Karena kamu tidak punya API backend, kita anggap update berhasil (dummy)
        setLoading(true)
        // simulasi kecil (tanpa delay) lalu langsung sukses
        setLoading(false)
        Toast.makeText(requireContext(), "Produk berhasil diperbarui (dummy)", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
