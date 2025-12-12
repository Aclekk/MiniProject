package com.example.miniproject.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
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

    // simpan address hasil geocoder biar dipakai saat pay
    private var currentAddress: Address? = null

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

        binding.tvPrice.text = subtotal.toCurrency()
        binding.tvSubtotal.text = subtotal.toCurrency()
        binding.tvShipping.text = shippingCost.toCurrency()
        binding.tvShippingCost.text = shippingCost.toCurrency()
        binding.tvTotal.text = total.toCurrency()
        binding.tvTotalBottom.text = total.toCurrency()

        // default
        binding.rbTransfer.isChecked = true
        binding.rbTransfer.setOnClickListener { binding.rbEwallet.isChecked = false }
        binding.rbEwallet.setOnClickListener { binding.rbTransfer.isChecked = false }

        binding.btnPayNow.setOnClickListener { processPayment() }
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

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                currentAddress = null
                binding.tvUserAddress.text = "Lokasi tidak ditemukan"
                binding.tvAddress.text = "📍 Aktifkan GPS Anda"
                return@addOnSuccessListener
            }

            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses: List<Address> =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) ?: emptyList()

                if (addresses.isNotEmpty()) {
                    val addr = addresses[0]
                    currentAddress = addr

                    val fullAddress = addr.getAddressLine(0) ?: ""
                    binding.tvUserAddress.text = fullAddress
                    binding.tvAddress.text =
                        "📍 Lat: ${String.format("%.4f", location.latitude)}, Lon: ${String.format("%.4f", location.longitude)}"

                    Log.d(
                        "CHECKOUT_ADDR",
                        "addr=$fullAddress city=${addr.locality} sub=${addr.subAdminArea} admin=${addr.adminArea} postal=${addr.postalCode}"
                    )
                } else {
                    currentAddress = null
                    binding.tvUserAddress.text = "Alamat tidak ditemukan"
                    binding.tvAddress.text = "📍 Coba nyalakan lokasi lebih akurat"
                }
            } catch (e: Exception) {
                currentAddress = null
                binding.tvUserAddress.text = "Gagal mendapatkan alamat"
                binding.tvAddress.text = "📍 ${e.localizedMessage}"
                Log.e("CHECKOUT_ADDR", "Geocoder failed", e)
            }
        }
    }

    private fun processPayment() {
        val prod = product ?: return

        val paymentMethod = if (binding.rbEwallet.isChecked) "ewallet" else "bank_transfer"

        val addr = currentAddress
        if (addr == null) {
            Toast.makeText(this, "Alamat belum siap. Tunggu GPS/Geocoder dulu.", Toast.LENGTH_LONG).show()
            return
        }

        val shippingAddress = (addr.getAddressLine(0) ?: "").trim()
        val city = (addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "").trim()
        val province = (addr.adminArea ?: "").trim()
        val postal = (addr.postalCode ?: "").trim()

        if (shippingAddress.isEmpty() || city.isEmpty() || province.isEmpty() || postal.isEmpty()) {
            Toast.makeText(
                this,
                "Alamat belum lengkap (city/province/postal kosong). Coba pindah lokasi / nyalain lokasi lebih akurat.",
                Toast.LENGTH_LONG
            ).show()
            Log.d("CHECKOUT_ADDR", "INVALID address='$shippingAddress' city='$city' prov='$province' postal='$postal'")
            return
        }

        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val jwt = sp.getString("token", null)
        if (jwt.isNullOrEmpty()) {
            Toast.makeText(this, "Session login hilang. Silakan login ulang.", Toast.LENGTH_LONG).show()
            return
        }

        val recipientName = (sp.getString("full_name", "") ?: "").trim()
        val recipientPhone = (sp.getString("phone", "") ?: "").trim()
        if (recipientName.isEmpty() || recipientPhone.isEmpty()) {
            Toast.makeText(this, "Profil belum lengkap (nama/telepon). Isi dulu di profile.", Toast.LENGTH_LONG).show()
            return
        }

        val req = CheckoutRequest(
            product_id = prod.id,
            quantity = quantity,
            shipping_address = shippingAddress,
            shipping_city = city,
            shipping_province = province,
            shipping_postal_code = postal,
            recipient_name = recipientName,
            recipient_phone = recipientPhone,
            shipping_cost = shippingCost,
            note = null,
            payment_method = paymentMethod
        )

        Log.d("PAYMENT_DEBUG", "payment_method=$paymentMethod rbEwallet=${binding.rbEwallet.isChecked}")
        Log.d("CHECKOUT_REQ", "req=$req")

        lifecycleScope.launch {
            try {
                binding.btnPayNow.isEnabled = false
                binding.btnPayNow.text = "Memproses..."

                val resp = ApiClient.apiService.checkout(
                    token = "Bearer $jwt",
                    request = req
                )

                if (resp.success) {
                    val apiData = resp.data
                    val subtotal = prod.price * quantity
                    val total = subtotal + shippingCost

                    val newOrder = Order(
                        id = apiData?.orderId ?: (CartManager.orders.size + 1),
                        products = listOf(prod),
                        totalPrice = total,
                        status = apiData?.status ?: "pending",
                        paymentMethod = paymentMethod,
                        address = shippingAddress
                    )

                    CartManager.orders.add(newOrder)
                    if (isFromCart) CartManager.cartItems.remove(prod)

                    Toast.makeText(this@CheckoutActivity, "Pesanan berhasil dibuat ✅", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@CheckoutActivity, resp.message ?: "Gagal membuat pesanan", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("CHECKOUT_FAIL", "err", e)
                Toast.makeText(this@CheckoutActivity, "Gagal konek server: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                currentAddress = null
                binding.tvUserAddress.text = "Izin lokasi ditolak"
                binding.tvAddress.text = "📍 Berikan izin untuk mendapatkan lokasi"
            }
        }
    }
}
