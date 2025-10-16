package com.example.miniproject.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.miniproject.R
import com.example.miniproject.data.OrderManager
import com.example.miniproject.model.DummyDataRepository
import com.example.miniproject.model.Order
import java.text.NumberFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    private lateinit var etShippingAddress: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var rgPaymentMethod: RadioGroup
    private lateinit var tvProductName: TextView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvQuantity: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvShippingCost: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnConfirmPayment: Button

    private var productId: String? = null
    private var productName: String? = null
    private var productPrice: Double = 0.0
    private var quantity: Int = 1
    private val shippingCost = 20000.0 // Biaya pengiriman tetap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        getIntentData()
        displayOrderSummary()
        setupButtons()
    }

    private fun initViews() {
        etShippingAddress = findViewById(R.id.etShippingAddress)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod)
        tvProductName = findViewById(R.id.tvProductName)
        tvProductPrice = findViewById(R.id.tvProductPrice)
        tvQuantity = findViewById(R.id.tvQuantity)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvShippingCost = findViewById(R.id.tvShippingCost)
        tvTotal = findViewById(R.id.tvTotal)
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment)
    }

    private fun getIntentData() {
        productId = intent.getStringExtra("PRODUCT_ID")
        productName = intent.getStringExtra("PRODUCT_NAME")
        productPrice = intent.getDoubleExtra("PRODUCT_PRICE", 0.0)
        quantity = intent.getIntExtra("QUANTITY", 1)
    }

    private fun displayOrderSummary() {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val subtotal = productPrice * quantity
        val total = subtotal + shippingCost

        tvProductName.text = productName
        tvProductPrice.text = currencyFormat.format(productPrice)
        tvQuantity.text = "x$quantity"
        tvSubtotal.text = currencyFormat.format(subtotal)
        tvShippingCost.text = currencyFormat.format(shippingCost)
        tvTotal.text = currencyFormat.format(total)
    }

    private fun setupButtons() {
        btnConfirmPayment.setOnClickListener {
            processCheckout()
        }

        findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun processCheckout() {
        val address = etShippingAddress.text.toString().trim()
        val phone = etPhoneNumber.text.toString().trim()
        val selectedPaymentId = rgPaymentMethod.checkedRadioButtonId

        // Validasi
        if (address.isEmpty()) {
            etShippingAddress.error = "Alamat pengiriman harus diisi"
            etShippingAddress.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            etPhoneNumber.error = "Nomor telepon harus diisi"
            etPhoneNumber.requestFocus()
            return
        }

        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentMethod = findViewById<RadioButton>(selectedPaymentId).text.toString()

        // Show loading
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Memproses pembayaran...")
            setCancelable(false)
            show()
        }

        // Simulasi proses pembayaran (2 detik)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            progressDialog.dismiss()
            createOrder(address, phone, paymentMethod)
        }, 2000)
    }

    private fun createOrder(address: String, phone: String, paymentMethod: String) {
        val orderId = "ORD${System.currentTimeMillis()}"
        val currentUserId = "USER001" // Simulasi user login
        val currentUserName = "Pembeli User" // Dalam real app, ambil dari session

        val product = DummyDataRepository.products.find { it.id == productId }

        if (product == null) {
            Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val subtotal = productPrice * quantity
        val total = subtotal + shippingCost

        // ✅ Buat produk dengan quantity yang sesuai
        val productWithQuantity = product.copy(quantity = quantity)

        val newOrder = Order(
            id = orderId,
            userId = currentUserId,
            userName = currentUserName,
            items = listOf("$productName x$quantity"),
            products = List(quantity) { productWithQuantity },
            status = "Menunggu Konfirmasi", // Status awal setelah user bayar
            totalPrice = total,
            orderDate = System.currentTimeMillis(),
            paymentStatus = "PAID",
            shippingAddress = address,
            phoneNumber = phone,
            paymentMethod = paymentMethod
        )

        // Simpan order ke OrderManager (akan sync ke semua tempat)
        OrderManager.addOrder(newOrder)

        // Tampilkan dialog sukses
        showSuccessDialog(orderId, total)
    }

    private fun showSuccessDialog(orderId: String, total: Double) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        AlertDialog.Builder(this)
            .setTitle("✅ Pembayaran Berhasil!")
            .setMessage(
                """
                Terima kasih! Pembayaran Anda berhasil.
                
                Order ID: $orderId
                Total Bayar: ${currencyFormat.format(total)}
                
                Status: Menunggu Konfirmasi Penjual
                
                Pesanan Anda akan segera diproses. Anda dapat melacak status pesanan di menu "Pesanan Saya".
                """.trimIndent()
            )
            .setPositiveButton("OK") { _, _ ->
                finish() // Kembali ke halaman sebelumnya
            }
            .setCancelable(false)
            .show()
    }
}