package com.example.miniproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.miniproject.databinding.FragmentOrdersBinding
import com.google.android.material.tabs.TabLayoutMediator

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        val prefs = requireActivity().getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val role = (prefs.getString("role", "buyer") ?: "buyer").lowercase()

        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2

            override fun createFragment(position: Int): Fragment {
                val isSeller = (role == "seller" || role == "admin")

                return if (isSeller) {
                    // SELLER/ADMIN
                    if (position == 0) AdminOrderListFragment() else AdminOrderHistoryFragment()
                } else {
                    // BUYER
                    if (position == 0) ActiveOrdersFragment() else CompletedOrdersFragment()
                }
            }
        }

        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Pesanan Aktif" else "Riwayat Pesanan"
        }.attach()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
