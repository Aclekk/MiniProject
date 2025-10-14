package com.example.miniproject.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.ActivityCheckoutBinding
import com.example.miniproject.model.Product
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 🔹 Ambil lokasi otomatis saat activity dibuka
        requestLocationPermission()

        // 🔹 Hitung total belanja (harga produk + ongkir)
        val total = CartManager.cartItems.sumOf { it.price } + 20000
        binding.tvTotal.text = "Rp ${String.format("%,d", total.toInt())}"

        // 🔹 Tombol Bayar Sekarang
        binding.btnPayNow.setOnClickListener {
            val selectedPayment = when {
                binding.rbTransfer.isChecked -> "Transfer Bank"
                binding.rbEwallet.isChecked -> "E-Wallet"
                else -> "Belum Dipilih"
            }

            if (selectedPayment == "Belum Dipilih") {
                Toast.makeText(this, "Pilih metode pembayaran dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 Ambil data produk dari intent
            val product = intent.getParcelableExtra<Product>("product")
            val quantity = intent.getIntExtra("quantity", 1)
            val totalPrice = (product?.price ?: 0.0) * quantity + 20000 // + ongkir

            if (product != null) {
                // ✅ Buat order baru dan simpan ke CartManager
                val newOrder = Order(
                    id = (CartManager.orders.size + 1),
                    products = listOf(product),
                    totalPrice = totalPrice,
                    status = "Dikemas",
                    paymentMethod = selectedPayment,
                    address = binding.tvUserAddress.text.toString()
                )

                CartManager.orders.add(newOrder)
                CartManager.clearCart() // Kosongkan keranjang

                Toast.makeText(
                    this,
                    "Pembayaran berhasil! Pesanan sedang dikemas.",
                    Toast.LENGTH_LONG
                ).show()
                finish() // Kembali ke CartFragment
            }
        }
    }

    // 🔹 Fungsi minta izin lokasi
    private fun requestLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak 😢", Toast.LENGTH_SHORT).show()
            }
        }

        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // 🔹 Fungsi ambil lokasi user
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val addressText = addresses?.firstOrNull()?.getAddressLine(0)

                    // tampilkan alamat lengkap jika tersedia, fallback ke koordinat
                    binding.tvUserAddress.text = addressText
                        ?: "📍 ${location.latitude}, ${location.longitude}"
                } catch (e: Exception) {
                    binding.tvUserAddress.text = "📍 ${location.latitude}, ${location.longitude}"
                }
            } else {
                binding.tvUserAddress.text = "Tidak dapat menemukan lokasi 😢"
            }
        }
    }
}
