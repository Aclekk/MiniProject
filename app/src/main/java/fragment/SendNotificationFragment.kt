package com.example.miniproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miniproject.data.NotificationManager
import com.example.miniproject.databinding.FragmentSendNotificationBinding

class SendNotificationFragment : Fragment() {

    private var _binding: FragmentSendNotificationBinding? = null
    private val binding get() = _binding!!

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

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSendNotification.setOnClickListener {
            val title = binding.etNotificationTitle.text.toString().trim()
            val message = binding.etNotificationMessage.text.toString().trim()

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "⚠️ Judul dan pesan tidak boleh kosong!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Kirim notifikasi
            NotificationManager.sendNotification(title, message)

            Toast.makeText(
                requireContext(),
                "✅ Notifikasi berhasil dikirim ke semua pengguna!",
                Toast.LENGTH_LONG
            ).show()

            // Clear input
            binding.etNotificationTitle.text?.clear()
            binding.etNotificationMessage.text?.clear()

            // Kembali ke ProfileFragment
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}