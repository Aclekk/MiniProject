package com.example.miniproject.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.miniproject.R
import com.example.miniproject.databinding.ActivityCheckoutBinding
import com.example.miniproject.model.Product
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Order
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private var product: Product? = null
    private var quantity: Int = 1
    private val shippingCost = 20000.0
    private var isFromCart: Boolean = false // 🆕 Deteksi sumber checkout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 🔹 Deteksi apakah dari Cart atau Product Detail
        product = intent.getParcelableExtra("product")
        quantity = intent.getIntExtra("quantity", 1)
        isFromCart = intent.getBooleanExtra("from_cart", false)

        setupUI()
        requestLocationPermission()
    }

    private fun setupUI() {
        product?.let { prod ->
            binding.tvProductName.text = prod.name
            binding.tvQuantity.text = "x$quantity"

            val subtotal = prod.price * quantity
            val total = subtotal + shippingCost

            binding.tvPrice.text = "Rp ${String.format("%,d", subtotal.toInt())}"
            binding.tvSubtotal.text = "Rp ${String.format("%,d", subtotal.toInt())}"
            binding.tvShipping.text = "Rp ${String.format("%,d", shippingCost.toInt())}"
            binding.tvShippingCost.text = "Rp ${String.format("%,d", shippingCost.toInt())}"
            binding.tvTotal.text = "Rp ${String.format("%,d", total.toInt())}"
            binding.tvTotalBottom.text = "Rp ${String.format("%,d", total.toInt())}"

            // Default payment method
            binding.rbTransfer.isChecked = true

            // 🆕 FIX: Radio button manual listener
            binding.rbTransfer.setOnClickListener {
                binding.rbEwallet.isChecked = false
            }
            binding.rbEwallet.setOnClickListener {
                binding.rbTransfer.isChecked = false
            }

            binding.btnPayNow.setOnClickListener {
                processPayment()
            }
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getUserLocation()
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val fullAddress = "${address.getAddressLine(0)}"
                            binding.tvUserAddress.text = fullAddress
                            binding.tvAddress.text = "📍 Lat: ${String.format("%.4f", location.latitude)}, " +
                                    "Lon: ${String.format("%.4f", location.longitude)}"
                        }
                    } catch (e: Exception) {
                        binding.tvUserAddress.text = "Gagal mendapatkan alamat"
                        binding.tvAddress.text = "📍 ${e.message}"
                    }
                } else {
                    binding.tvUserAddress.text = "Lokasi tidak ditemukan"
                    binding.tvAddress.text = "📍 Aktifkan GPS Anda"
                }
            }
        }
    }

    private fun processPayment() {
        val selectedPaymentMethod = when {
            binding.rbTransfer.isChecked -> "Transfer Bank"
            binding.rbEwallet.isChecked -> "E-Wallet"
            else -> {
                Toast.makeText(
                    this,
                    "Pilih metode pembayaran terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        product?.let { prod ->
            val subtotal = prod.price * quantity
            val total = subtotal + shippingCost
            val userAddress = binding.tvUserAddress.text.toString()

            // 🆕 Buat pesanan baru dan masukkan ke CartManager
            val newOrder = Order(
                id = CartManager.orders.size + 1,
                products = listOf(prod),
                totalPrice = total,
                status = "Dikemas", // Status awal setelah bayar
                paymentMethod = selectedPaymentMethod,
                address = userAddress
            )

            CartManager.orders.add(newOrder)

            // 🆕 FIX: Hapus barang dari cart kalau checkout dari cart
            if (isFromCart) {
                CartManager.cartItems.remove(prod)
            }

            Toast.makeText(
                this,
                "Pesanan berhasil! Metode: $selectedPaymentMethod\nTotal: Rp ${String.format("%,d", total.toInt())}",
                Toast.LENGTH_LONG
            ).show()

            binding.btnPayNow.isEnabled = false
            binding.btnPayNow.text = "Memproses..."

            binding.btnPayNow.postDelayed({
                Toast.makeText(this, "Pembayaran berhasil! ✅", Toast.LENGTH_SHORT).show()
                finish()
            }, 2000)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                binding.tvUserAddress.text = "Izin lokasi ditolak"
                binding.tvAddress.text = "📍 Berikan izin untuk mendapatkan lokasi"
            }
        }
    }
}