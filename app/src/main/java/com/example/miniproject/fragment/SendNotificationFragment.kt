package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miniproject.adapter.NotificationHistoryAdapter
import com.example.miniproject.data.NotificationManager
import com.example.miniproject.databinding.FragmentSendNotificationBinding
import com.example.miniproject.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class SendNotificationFragment : Fragment() {
    private var _binding: FragmentSendNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationAdapter: NotificationHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupNotificationForm()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationHistoryAdapter(NotificationManager.notifications)
        binding.rvNotificationHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
        updateNotificationCount()
    }

    private fun setupNotificationForm() {
        binding.btnSendNotification.setOnClickListener {
            val title = binding.etNotificationTitle.text.toString().trim()
            val message = binding.etNotificationMessage.text.toString().trim()

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Judul dan pesan tidak boleh kosong!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val type = when {
                binding.rbPromo.isChecked -> "Promo"
                binding.rbInfo.isChecked -> "Info"
                binding.rbUpdate.isChecked -> "Update"
                else -> "Info"
            }

            // Buat notifikasi baru
            val notification = Notification(
                id = NotificationManager.notifications.size + 1,
                title = title,
                message = message,
                type = type,
                createdAt = Date()
            )

            // Simpan notifikasi
            NotificationManager.addNotification(notification)

            // Refresh list
            notificationAdapter.notifyDataSetChanged()
            updateNotificationCount()

            // Reset form
            binding.etNotificationTitle.text?.clear()
            binding.etNotificationMessage.text?.clear()
            binding.radioGroupType.clearCheck()

            Toast.makeText(
                requireContext(),
                "✅ Notifikasi berhasil dikirim ke semua pengguna!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateNotificationCount() {
        binding.tvNotificationCount.text = "📨 Total Notifikasi Terkirim: ${NotificationManager.notifications.size}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}