package com.example.miniproject.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.data.api.ApiClient
import com.example.miniproject.data.model.CheckoutRequest
import com.example.miniproject.databinding.ActivityCheckoutBinding
import com.example.miniproject.model.Product
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val LOCATION_TIMEOUT_MS = 10000L // 10 detik timeout

    private var product: Product? = null
    private var quantity: Int = 1
    private val shippingCost = 20000.0
    private var isFromCart: Boolean = false

    private var currentAddress: Address? = null
    private var isLocationLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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

        fun Double.toCurrency(): String = "Rp ${String.format("%,d", this.toInt())}"

        binding.tvSubtotal.text = subtotal.toCurrency()
        binding.tvShipping.text = shippingCost.toCurrency()
        binding.tvTotal.text = total.toCurrency()
        binding.tvTotalBottom.text = total.toCurrency()

        binding.rbTransfer.isChecked = true
        binding.rbTransfer.setOnClickListener { binding.rbEwallet.isChecked = false }
        binding.rbEwallet.setOnClickListener { binding.rbTransfer.isChecked = false }

        binding.btnPayNow.setOnClickListener { processPayment() }

        // ✅ Default text while loading
        binding.tvUserAddress.text = "📍 Mengambil lokasi Anda..."
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
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
                // Permission denied
                binding.tvUserAddress.text = "⚠️ Izin lokasi ditolak. Isi alamat manual di bawah."
                Toast.makeText(
                    this,
                    "Izin lokasi diperlukan untuk auto-fill alamat",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            binding.tvUserAddress.text = "⚠️ Izin lokasi diperlukan"
            return
        }

        if (isLocationLoading) return
        isLocationLoading = true

        // ✅ Show loading
        binding.tvUserAddress.text = "📍 Mengambil lokasi... (max 10 detik)"

        // ✅ STEP 1: Try last known location first (instant)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("CHECKOUT", "✅ Using last known location (instant)")
                processLocation(location.latitude, location.longitude)
                isLocationLoading = false
            } else {
                // ✅ STEP 2: Request fresh location (GPS active)
                Log.d("CHECKOUT", "⏳ Last location null, requesting fresh location...")
                requestFreshLocation()
            }
        }.addOnFailureListener { e ->
            Log.e("CHECKOUT", "❌ Last location failed: ${e.message}")
            requestFreshLocation()
        }
    }

    private fun requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L // 5 detik interval
        ).apply {
            setMaxUpdates(1) // ✅ Cuma 1x update, langsung stop
            setWaitForAccurateLocation(false) // ✅ Tidak tunggu super akurat
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Log.d("CHECKOUT", "✅ Fresh location received")
                    processLocation(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this)
                    isLocationLoading = false
                }
            }
        }

        // ✅ TIMEOUT: Cancel jika 10 detik tidak dapat lokasi
        Handler(Looper.getMainLooper()).postDelayed({
            if (isLocationLoading) {
                Log.e("CHECKOUT", "⏱️ Location timeout!")
                fusedLocationClient.removeLocationUpdates(locationCallback)
                isLocationLoading = false

                binding.tvUserAddress.text = "⚠️ Lokasi tidak tersedia (timeout). Isi alamat manual:"
                Toast.makeText(
                    this,
                    "GPS timeout. Pastikan GPS aktif atau isi alamat manual.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, LOCATION_TIMEOUT_MS)

        // ✅ Start location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun processLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                currentAddress = addresses[0]
                val addressLine = addresses[0].getAddressLine(0)
                binding.tvUserAddress.text = "✅ $addressLine"

                Log.d("CHECKOUT", "✅ Address: $addressLine")
                Toast.makeText(this, "Alamat berhasil diambil", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvUserAddress.text = "⚠️ Alamat tidak ditemukan. Isi manual."
                Log.e("CHECKOUT", "❌ Geocoder returned empty")
            }
        } catch (e: Exception) {
            Log.e("CHECKOUT", "❌ Geocoding error: ${e.message}")
            binding.tvUserAddress.text = "⚠️ Error mengambil alamat. Isi manual."
            Toast.makeText(this, "Error geocoding: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processPayment() {
        val prod = product ?: return

        // ✅ Allow manual address input if currentAddress is null
        val addr = currentAddress

        if (addr == null) {
            Toast.makeText(
                this,
                "Alamat belum tersedia.\n\n" +
                        "Pastikan:\n" +
                        "1. GPS aktif\n" +
                        "2. Izin lokasi granted\n" +
                        "3. Atau implementasi input manual alamat",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = sp.getString("token", null) ?: run {
            Toast.makeText(this, "Token tidak ditemukan, login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CheckoutRequest(
            product_id = prod.id,
            quantity = quantity,
            shipping_address = addr.getAddressLine(0) ?: "",
            shipping_city = addr.locality ?: addr.subAdminArea ?: "",
            shipping_province = addr.adminArea ?: "",
            shipping_postal_code = addr.postalCode ?: "",
            recipient_name = sp.getString("full_name", "") ?: "",
            recipient_phone = sp.getString("phone", "") ?: "",
            shipping_cost = shippingCost,
            note = null,
            payment_method = if (binding.rbEwallet.isChecked) "ewallet" else "bank_transfer"
        )

        // ✅ Disable button during checkout
        binding.btnPayNow.isEnabled = false
        binding.btnPayNow.text = "Memproses..."

        lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.checkout(
                    "Bearer $token",
                    request
                )

                if (resp.success) {
                    Toast.makeText(
                        this@CheckoutActivity,
                        "✅ Order berhasil dibuat!",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@CheckoutActivity,
                        "❌ ${resp.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.btnPayNow.isEnabled = true
                    binding.btnPayNow.text = "Bayar Sekarang"
                }
            } catch (e: Exception) {
                Log.e("CHECKOUT", "❌ Checkout error", e)
                Toast.makeText(
                    this@CheckoutActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnPayNow.isEnabled = true
                binding.btnPayNow.text = "Bayar Sekarang"
            }
        }
    }
}