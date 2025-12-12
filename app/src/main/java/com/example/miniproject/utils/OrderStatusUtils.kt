package com.example.miniproject.util

/**
 * STATUS INTERNAL (DB) yang kita pakai:
 * pending -> packed -> shipped -> completed
 * cancelled
 */

fun normalizeDbStatus(s: String?): String {
    val raw = (s ?: "").trim().lowercase()
    return when (raw) {
        "dikemas" -> "packed"
        "dikirim" -> "shipped"
        "selesai" -> "completed"
        "dibatalkan" -> "cancelled"
        "processing" -> "packed" // kalau masih ada legacy
        "" -> "pending"
        else -> raw
    }
}

fun statusLabel(dbStatus: String?): String {
    return when (normalizeDbStatus(dbStatus)) {
        "pending" -> "Pending"
        "packed" -> "Dikemas"
        "shipped" -> "Dikirim"
        "completed" -> "Selesai"
        "cancelled" -> "Dibatalkan"
        else -> dbStatus ?: "-"
    }
}

fun nextStatusForSeller(current: String?): String? {
    return when (normalizeDbStatus(current)) {
        "pending" -> "packed"
        "packed" -> "shipped"
        // seller STOP di shipped
        else -> null
    }
}

fun nextStatusForBuyer(current: String?): String? {
    return when (normalizeDbStatus(current)) {
        "shipped" -> "completed"
        else -> null
    }
}

fun isActiveStatusForSeller(status: String?): Boolean {
    return normalizeDbStatus(status) !in setOf("completed", "cancelled")
}

fun isActiveStatusForBuyer(status: String?): Boolean {
    return normalizeDbStatus(status) !in setOf("completed", "cancelled")
}
