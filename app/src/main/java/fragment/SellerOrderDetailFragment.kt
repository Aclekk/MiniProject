package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.miniproject.databinding.FragmentOrderDetailBinding
import com.example.miniproject.data.api.ApiClient
import kotlinx.coroutines.launch

class SellerOrderDetailFragment : Fragment() {

    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val TAG = "SELLER_ORDER_DETAIL"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderId = arguments?.getInt("orderId") ?: 0
        if (orderId == 0) {
            Toast.makeText(requireContext(), "Order ID tidak valid", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        // ❌ SELLER TIDAK BOLEH ADA BUTTON
        binding.btnNextStatus.visibility = View.GONE
        binding.btnRateNow.visibility = View.GONE

        loadOrderDetail(orderId)
    }

    private fun loadOrderDetail(orderId: Int) {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token kosong. Login ulang.", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.getOrderDetail(
                    token = "Bearer $token",
                    orderId = orderId
                )

                Log.d(TAG, "HTTP ${resp.code()}")
                Log.d(TAG, "RAW BODY = ${resp.body()}")

                if (!resp.isSuccessful || resp.body()?.success != true) {
                    Toast.makeText(requireContext(), "Gagal memuat detail order", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val data = resp.body()?.data
                if (data == null) {
                    Toast.makeText(requireContext(), "Data order kosong", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                renderOrderDetail(data)

            } catch (e: Exception) {
                Log.e(TAG, "ERROR loadOrderDetail", e)
                Toast.makeText(requireContext(), e.localizedMessage ?: "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderOrderDetail(data: Any) {
        /**
         * Struktur backend diasumsikan:
         *
         * data.order.id
         * data.order.status
         * data.order.payment_method
         * data.order.shipping_address
         * data.order.total_price
         *
         * data.items[].product_name
         * data.items[].qty
         */

        val order = data.javaClass.getField("order").get(data)
        val items = data.javaClass.getField("items").get(data) as List<*>

        val orderClass = order.javaClass

        val id = orderClass.getField("id").get(order)
        val status = orderClass.getField("status").get(order)
        val paymentMethod = orderClass.getField("payment_method").get(order)
        val address = orderClass.getField("shipping_address").get(order)
        val totalPrice = orderClass.getField("total_price").get(order)

        binding.tvOrderId.text = "Order #$id"
        binding.tvOrderStatus.text = "Status: ${status ?: "-"}"
        binding.tvOrderPayment.text = "Metode Pembayaran: ${paymentMethod ?: "-"}"
        binding.tvOrderAddress.text = "Alamat: ${address ?: "-"}"

        val productText = items.joinToString("\n") { item ->
            val cls = item!!.javaClass
            val name = cls.getField("product_name").get(item)
            val qty = cls.getField("qty").get(item)
            "- $name x$qty"
        }

        binding.tvOrderProducts.text =
            if (productText.isNotBlank()) "Produk:\n$productText"
            else "Produk: -"

        val totalFormatted = try {
            String.format("%,d", (totalPrice as Number).toInt())
        } catch (_: Exception) {
            "0"
        }

        binding.tvOrderTotal.text = "Total: Rp $totalFormatted"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
