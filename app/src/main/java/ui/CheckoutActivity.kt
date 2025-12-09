package com.example.miniproject.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Order
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.api.CheckoutRequest
import com.example.miniproject.databinding.ActivityCheckoutBinding
import com.example.miniproject.model.Product
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private var product: Product? = null
    private var quantity: Int = 1
    private val shippingCost = 20000.0
    private var isFromCart: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Data dari Intent
        product = intent.getParcelableExtra("product")
        quantity = intent.getIntExtra("quantity", 1)
        isFromCart = intent.getBooleanExtra("from_cart", false)

        setupUI()
        requestLocationPermission()
    }

    private fun setupUI() {
        val prod = product ?: return

        binding.tvProductName.text = prod.name
        binding.tvQuantity.text = "x$quantity"

        val subtotal = prod.price * quantity
        val total = subtotal + shippingCost

        fun Double.toCurrency(): String =
            "Rp ${String.format("%,d", this.toInt())}"

        binding.tvPrice.text = subtotal.toCurrency()
        binding.tvSubtotal.text = subtotal.toCurrency()
        binding.tvShipping.text = shippingCost.toCurrency()
        binding.tvShippingCost.text = shippingCost.toCurrency()
        binding.tvTotal.text = total.toCurrency()
        binding.tvTotalBottom.text = total.toCurrency()

        // Default payment method
        binding.rbTransfer.isChecked = true
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
                            val fullAddress = address.getAddressLine(0) ?: ""
                            binding.tvUserAddress.text = fullAddress
                            binding.tvAddress.text =
                                "📍 Lat: ${String.format("%.4f", location.latitude)}, " +
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
        val paymentMethod = when {
            binding.rbTransfer.isChecked -> "Transfer Bank"
            binding.rbEwallet.isChecked -> "E-Wallet"
            else -> {
                Toast.makeText(this, "Pilih metode pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val prod = product ?: return

        // 🔥 ambil FCM token dari SharedPreferences
        val sp = getSharedPreferences("user_pref", MODE_PRIVATE)
        val fcmToken = sp.getString("fcm_token", null)

        if (fcmToken.isNullOrEmpty()) {
            Toast.makeText(this, "FCM token kosong. Coba login ulang dulu.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.btnPayNow.isEnabled = false
                binding.btnPayNow.text = "Memproses..."

                // 🔥 call barbar tanpa Authorization
                val resp = ApiClient.apiService.checkout(
                    CheckoutRequest(
                        product_id = prod.id,
                        quantity = quantity,
                        fcm_token = fcmToken
                    )
                )

                if (resp.success) {
                    val apiData = resp.data   // CreateOrderResult?

                    val subtotal = prod.price * quantity
                    val total = subtotal + shippingCost
                    val userAddress = binding.tvUserAddress.text.toString()

                    val newOrder = Order(
                        id = apiData?.orderId ?: (CartManager.orders.size + 1),
                        products = listOf(prod),
                        totalPrice = total,
                        status = apiData?.status ?: "pending",
                        paymentMethod = paymentMethod,
                        address = userAddress
                    )

                    CartManager.orders.add(newOrder)

                    if (isFromCart) {
                        CartManager.cartItems.remove(prod)
                    }

                    Toast.makeText(
                        this@CheckoutActivity,
                        "Pesanan berhasil dibuat. Seller dapat notifikasi 🚚",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                } else {
                    Toast.makeText(
                        this@CheckoutActivity,
                        resp.message ?: "Gagal membuat pesanan",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CheckoutActivity,
                    "Gagal konek server: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnPayNow.isEnabled = true
                binding.btnPayNow.text = "Bayar Sekarang"
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                getUserLocation()
            } else {
                binding.tvUserAddress.text = "Izin lokasi ditolak"
                binding.tvAddress.text = "📍 Berikan izin untuk mendapatkan lokasi"
            }
        }
    }
}
