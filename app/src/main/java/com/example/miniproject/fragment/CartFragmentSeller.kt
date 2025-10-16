package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.miniproject.databinding.FragmentCartSellerBinding

class CartFragmentSeller : Fragment() {
    private var _binding: FragmentCartSellerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Info untuk seller bahwa ini bukan halaman cart biasa
        binding.tvInfo.text = """
            📦 Halaman Keranjang Seller
            
            Sebagai penjual, Anda tidak memiliki keranjang belanja.
            Gunakan menu berikut:
            
            • Kelola Pesanan - untuk melihat pesanan masuk
            • Kelola Produk - untuk menambah/edit produk
            • Laporan Penjualan - untuk melihat statistik
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}