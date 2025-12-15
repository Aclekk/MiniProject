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

        // 🔒 SELLER = READ ONLY (semua button hilang)
        binding.btnNextStatus.visibility = View.GONE
        binding.btnRateNow.visibility = View.GONE

        loadOrderDetail(orderId)
    }

    private fun loadOrderDetail(orderId: Int) {
        val prefs = requireContext().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token kosong", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.getOrderDetail(
                    token = "Bearer $token",
                    orderId = orderId
                )

                if (!resp.isSuccessful || resp.body()?.success != true) {
                    Toast.makeText(requireContext(), "Gagal memuat detail order", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val data = resp.body()?.data ?: return@launch

                // ✅ Akses data sebagai Map (aman untuk JSON dynamic)
                val orderMap = data.order as? Map<*, *> ?: emptyMap<String, Any>()
                val items = data.items ?: emptyList()

                // ✅ Render data dengan fallback aman
                binding.tvOrderId.text = "🧾 Order #$orderId"
                binding.tvOrderStatus.text = "📦 Status: ${orderMap["status"] ?: "-"}"
                binding.tvOrderPayment.text = "💳 Metode Pembayaran: ${orderMap["payment_method"] ?: "-"}"
                binding.tvOrderAddress.text = "📍 Alamat: ${orderMap["address"] ?: "-"}"

                val totalPrice = orderMap["total_price"]?.toString() ?: "0"
                binding.tvOrderTotal.text = "💰 Total: Rp $totalPrice"

                // ✅ Build list produk dari items
                val productText = items.joinToString("\n") { item ->
                    val itemMap = item as? Map<*, *> ?: emptyMap<String, Any>()
                    val name = itemMap["product_name"] ?: "Produk"
                    val qty = itemMap["quantity"] ?: "1"
                    "- $name x$qty"
                }

                binding.tvOrderProducts.text = if (productText.isBlank()) {
                    "🛒 Produk:\n-"
                } else {
                    "🛒 Produk:\n$productText"
                }

            } catch (e: Exception) {
                Log.e("SELLER_DETAIL", "Error: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}