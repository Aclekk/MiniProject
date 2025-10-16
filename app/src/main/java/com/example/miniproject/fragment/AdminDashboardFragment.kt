// INI UNTUK FILE: AdminDashboardFragment.kt

package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.miniproject.R
import com.example.miniproject.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardManajemenPesanan.setOnClickListener {
            navigateTo(OrderManagementFragment())
        }
        binding.cardLaporanPenjualan.setOnClickListener {
            navigateTo(SalesReportFragment())
        }
        binding.cardUlasanPembeli.setOnClickListener {
            navigateTo(ReviewManagementFragment())
        }
        binding.cardKirimNotifikasi.setOnClickListener {
            navigateTo(SendNotificationFragment())
        }
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}