package com.example.miniproject.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.location.Location
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.miniproject.data.CartManager
import com.example.miniproject.data.Order
import com.example.miniproject.databinding.ActivityCheckoutBinding
import com.example.miniproject.model.Product
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentProduct: Product? = null
    private var quantity: Int = 1
    private var currentLocation: String = "Memuat lokasi..."

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        currentProduct = intent.getParcelableExtra("product")
        quantity = intent.getIntExtra("quantity", 1)

        // Ambil lokasi otomatis (dummy)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            currentLocation = if (location != null) {
                "Lat: ${location.latitude}, Lng: ${location.longitude}"
            } else {
                "Lokasi tidak tersedia"
            }
            binding.tvUserAddress.text = currentLocation
        }

        setupUI()
    }

    private fun setupUI() {
        val product = currentProduct ?: return

        val totalHarga = product.price * quantity
        val ongkir = 20000.0
        val totalPembayaran = totalHarga + ongkir

        binding.tvProductName.text = product.name
        binding.tvQuantity.text = "x$quantity"
        binding.tvPrice.text = "Rp ${String.format("%,d", product.price.toInt())}"
        binding.tvShipping.text = "Rp 20.000"
        binding.tvTotal.text = "Rp ${String.format("%,d", totalPembayaran.toInt())}"

        binding.btnPayNow.setOnClickListener {
            val selectedId = binding.radioGroupPayment.checkedRadioButtonId
            val selectedMethod =
                if (selectedId != -1)
                    findViewById<RadioButton>(selectedId).text.toString()
                else "Belum dipilih"

            val newOrder = Order(
                id = (CartManager.orders.size + 1),
                products = listOf(product),
                totalPrice = totalPembayaran,
                status = "Belum Bayar",
                paymentMethod = selectedMethod,
                address = currentLocation
            )

            // 🔥 Dialog konfirmasi realistis
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pembayaran")
                .setMessage("Apakah kamu sudah melakukan pembayaran?")
                .setPositiveButton("Ya, Sudah Bayar") { _, _ ->
                    newOrder.status = "Dikemas"
                    CartManager.orders.add(newOrder)
                    CartManager.clearCart()
                    Toast.makeText(this, "Pesanan berhasil! Status: Dikemas", Toast.LENGTH_LONG).show()
                    finish()
                }
                .setNegativeButton("Belum") { _, _ ->
                    newOrder.status = "Belum Bayar"
                    CartManager.orders.add(newOrder)
                    CartManager.clearCart()
                    Toast.makeText(this, "Pesanan tersimpan sebagai Belum Bayar", Toast.LENGTH_LONG).show()
                    finish()
                }
                .show()
        }
    }
}
