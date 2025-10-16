package com.example.miniproject.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.data.CartManager
import com.example.miniproject.model.DummyDataRepository
import com.example.miniproject.model.Product
import com.example.miniproject.ui.CheckoutActivity
import java.text.NumberFormat
import java.util.*

class ProductDetailFragment : Fragment() {
    private var product: Product? = null
    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get product from arguments
        val productId = arguments?.getString("productId") ?: arguments?.getString("PRODUCT_ID")
        product = DummyDataRepository.products.find { it.id == productId }

        if (product != null) {
            displayProductDetails(view)
            setupButtons(view)
        } else {
            Toast.makeText(requireContext(), "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayProductDetails(view: View) {
        product?.let { prod ->
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            // Set product details
            view.findViewById<TextView>(R.id.tvProductName)?.text = prod.name
            view.findViewById<TextView>(R.id.tvProductPrice)?.text = currencyFormat.format(prod.price)
            view.findViewById<TextView>(R.id.tvProductDescription)?.text = prod.description
            view.findViewById<TextView>(R.id.tvProductCategory)?.text = "Kategori: ${prod.category}"
            view.findViewById<TextView>(R.id.tvQuantity)?.text = quantity.toString()

            // Set image if available (you can use Glide or Picasso)
            // Glide.with(this).load(prod.imageUrl).into(view.findViewById(R.id.ivProductImage))
        }
    }

    private fun setupButtons(view: View) {
        // Button Plus
        view.findViewById<Button>(R.id.btnPlus)?.setOnClickListener {
            quantity++
            view.findViewById<TextView>(R.id.tvQuantity)?.text = quantity.toString()
        }

        // Button Minus
        view.findViewById<Button>(R.id.btnMinus)?.setOnClickListener {
            if (quantity > 1) {
                quantity--
                view.findViewById<TextView>(R.id.tvQuantity)?.text = quantity.toString()
            }
        }

        // Button Add to Cart
        view.findViewById<Button>(R.id.btnAddToCart)?.setOnClickListener {
            product?.let { prod ->
                CartManager.addToCart(prod, quantity)
                Toast.makeText(
                    requireContext(),
                    "✅ ${prod.name} ditambahkan ke keranjang",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Button Buy Now - Langsung ke Checkout
        view.findViewById<Button>(R.id.btnBuyNow)?.setOnClickListener {
            product?.let { prod ->
                goToCheckout(prod)
            }
        }
    }

    private fun goToCheckout(product: Product) {
        val intent = Intent(requireContext(), CheckoutActivity::class.java).apply {
            putExtra("PRODUCT_ID", product.id)
            putExtra("PRODUCT_NAME", product.name)
            putExtra("PRODUCT_PRICE", product.price)
            putExtra("QUANTITY", quantity)
            putExtra("IS_DIRECT_BUY", true) // Flag untuk beli langsung
        }
        startActivity(intent)
    }
}