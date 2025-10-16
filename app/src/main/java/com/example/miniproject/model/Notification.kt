data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long,
    val recipientCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis() // ✅ Tambahkan createdAt
)