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
import com.example.miniproject.data.model.CheckoutRequest
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

        binding.tvSubtotal.text = subtotal.toCurrency()
        binding.tvShipping.text = shippingCost.toCurrency()
        binding.tvTotal.text = total.toCurrency()
        binding.tvTotalBottom.text = total.toCurrency()

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
            if (location == null) return@addOnSuccessListener

            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                currentAddress = addresses[0]
                binding.tvUserAddress.text = addresses[0].getAddressLine(0)
            }
        }
    }

    private fun processPayment() {
        val prod = product ?: return
        val addr = currentAddress ?: run {
            Toast.makeText(this, "Alamat belum siap", Toast.LENGTH_LONG).show()
            return
        }

        val sp = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = sp.getString("token", null) ?: return

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

        lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.checkout(
                    "Bearer $token",
                    request
                )

                if (resp.success) {
                    Toast.makeText(this@CheckoutActivity, "Order berhasil dibuat", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@CheckoutActivity, resp.message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("CHECKOUT", "error", e)
                Toast.makeText(this@CheckoutActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}

//ini udah bisa buat dijalanin dan ganti status
//ada kendala di notif seller ga muncul pop up / bubble