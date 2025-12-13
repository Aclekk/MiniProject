package com.example.miniproject.util

/**
 * ✅ NORMALIZE: Semua status masuk jadi format internal yang konsisten
 * Database ENUM: pending, processing, shipped, completed, cancelled
 */
fun normalizeDbStatus(s: String?): String {
    val raw = (s ?: "").trim().lowercase()
    return when (raw) {
        // UI Indonesia
        "menunggu konfirmasi", "menunggu konfirmasi seller" -> "pending"
        "dikonfirmasi", "dikemas", "diproses" -> "processing"
        "dikirim" -> "shipped"
        "selesai" -> "completed"
        "dibatalkan", "batal" -> "cancelled"

        // Internal/legacy (biar backward compatible)
        "pending" -> "pending"
        "processing" -> "processing"
        "packed" -> "processing"  // ✅ LEGACY: packed -> processing
        "shipped" -> "shipped"
        "completed" -> "completed"
        "cancelled" -> "cancelled"

        // Kalau kosong atau null
        "", "null" -> "pending"

        // Fallback: return as-is (tapi ini seharusnya ga pernah kejadian)
        else -> raw
    }
}

/**
 * ✅ LABEL: Status yang ditampilkan ke user (Indonesia)
 */
fun statusLabel(dbStatus: String?): String {
    return when (normalizeDbStatus(dbStatus)) {
        "pending" -> "Menunggu Konfirmasi"
        "processing" -> "Dikonfirmasi"  // ✅ untuk seller & buyer
        "shipped" -> "Dikirim"
        "completed" -> "Selesai"
        "cancelled" -> "Dibatalkan"
        else -> dbStatus ?: "-"
    }
}

/**
 * ✅ NEXT STATUS untuk SELLER
 * pending -> processing (Konfirmasi)
 * processing -> shipped (Kirim)
 * shipped/completed/cancelled -> null (tidak bisa diubah)
 */
fun nextStatusForSeller(current: String?): String? {
    return when (normalizeDbStatus(current)) {
        "pending" -> "processing"      // ✅ Konfirmasi Pesanan
        "processing" -> "shipped"      // ✅ Kirim Pesanan
        else -> null                    // shipped/completed/cancelled = no action
    }
}

/**
 * ✅ NEXT STATUS untuk BUYER
 * shipped -> completed (Terima)
 * completed -> sudah selesai, bisa review
 */
fun nextStatusForBuyer(current: String?): String? {
    return when (normalizeDbStatus(current)) {
        "shipped" -> "completed"
        else -> null
    }
}

/**
 * ✅ BUTTON LABEL untuk SELLER
 */
fun sellerButtonLabel(dbStatus: String?): String? {
    return when (normalizeDbStatus(dbStatus)) {
        "pending" -> "📦 Konfirmasi Pesanan"
        "processing" -> "🚚 Kirim Pesanan"
        else -> null
    }
}

/**
 * ✅ BUTTON LABEL untuk BUYER
 */
fun buyerButtonLabel(dbStatus: String?): String? {
    return when (normalizeDbStatus(dbStatus)) {
        "shipped" -> "✅ Pesanan sudah diterima"
        "completed" -> "⭐ Kirim Ulasan"
        else -> null
    }
}