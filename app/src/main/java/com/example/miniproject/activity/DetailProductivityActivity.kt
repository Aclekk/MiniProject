package com.example.miniproject.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.miniproject.databinding.ActivityDetailProductBinding

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Memanggil fungsi onClick
        onClick()
    }

    private fun onClick() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        // --- BAGIAN YANG DIPERBAIKI ---
        // Sebelumnya: btn_tambah (Error: Unresolved reference)
        // Sekarang: Menggunakan 'binding.ivTambah' sesuai ID di XML
        binding.ivTambah.setOnClickListener {
            // TODO: Tambahkan logika untuk menambah jumlah barang di sini
        }

        // --- BAGIAN YANG DIPERBAIKI ---
        // Sebelumnya: btn_kurang (Error: Unresolved reference)
        // Sekarang: Menggunakan 'binding.ivKurang' sesuai ID di XML
        binding.ivKurang.setOnClickListener {
            // TODO: Tambahkan logika untuk mengurangi jumlah barang di sini
        }
    }
}