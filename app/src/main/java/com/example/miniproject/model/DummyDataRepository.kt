package com.example.miniproject.model

import com.example.miniproject.data.OrderManager

object DummyDataRepository {

    // Dummy Categories
    val categories = mutableListOf(
        Category("CAT001", "Alat Tanam", 50, System.currentTimeMillis()),
        Category("CAT002", "Pupuk & Nutrisi", 100, System.currentTimeMillis()),
        Category("CAT003", "Pestisida", 75, System.currentTimeMillis()),
        Category("CAT004", "Irigasi", 30, System.currentTimeMillis()),
        Category("CAT005", "Perlengkapan", 60, System.currentTimeMillis())
    )

    // Dummy Products (Alat Tani)
    val products = mutableListOf(
        Product("1", "Cangkul Baja", 150000.0, "Alat Tanam", "https://via.placeholder.com/300", "Cangkul baja berkualitas tinggi untuk mencangkul tanah"),
        Product("2", "Pupuk NPK 1kg", 35000.0, "Pupuk & Nutrisi", "https://via.placeholder.com/300", "Pupuk NPK lengkap untuk pertumbuhan optimal tanaman"),
        Product("3", "Sprayer 16L", 450000.0, "Pestisida", "https://via.placeholder.com/300", "Alat semprot pestisida kapasitas 16 liter"),
        Product("4", "Selang Irigasi 50m", 250000.0, "Irigasi", "https://via.placeholder.com/300", "Selang irigasi berkualitas panjang 50 meter"),
        Product("5", "Sarung Tangan Karet", 25000.0, "Perlengkapan", "https://via.placeholder.com/300", "Sarung tangan karet untuk melindungi tangan saat berkebun"),
        Product("6", "Sekop Taman", 85000.0, "Alat Tanam", "https://via.placeholder.com/300", "Sekop taman untuk menanam dan memindahkan tanah"),
        Product("7", "Pupuk Kompos 5kg", 50000.0, "Pupuk & Nutrisi", "https://via.placeholder.com/300", "Pupuk organik kompos untuk menyuburkan tanah"),
        Product("8", "Pestisida Organik", 75000.0, "Pestisida", "https://via.placeholder.com/300", "Pestisida organik ramah lingkungan")
    )

    // Orders akan diambil dari OrderManager
    val orders: MutableList<Order>
        get() = OrderManager.allOrders

    // Initialize dummy orders
    fun initializeDummyData() {
        if (OrderManager.allOrders.isEmpty()) {
            val dummyOrders = listOf(
                Order(
                    id = "ORD${System.currentTimeMillis() - 10000}",
                    userId = "USER001",
                    userName = "Budi Santoso",
                    items = listOf("Cangkul Baja x1"),
                    products = listOf(products[0]),
                    status = "Menunggu Konfirmasi",
                    totalPrice = 170000.0,
                    orderDate = System.currentTimeMillis() - (1000 * 60 * 30),
                    paymentStatus = "PAID",
                    shippingAddress = "Jl. Tani Makmur No. 123, Bogor",
                    phoneNumber = "08123456789",
                    paymentMethod = "Transfer Bank"
                ),
                Order(
                    id = "ORD${System.currentTimeMillis() - 20000}",
                    userId = "USER002",
                    userName = "Siti Aminah",
                    items = listOf("Pupuk NPK 1kg x2"),
                    products = listOf(products[1].copy(quantity = 2)),
                    status = "Dikemas",
                    totalPrice = 90000.0,
                    orderDate = System.currentTimeMillis() - (1000 * 60 * 60 * 2),
                    paymentStatus = "PAID",
                    shippingAddress = "Jl. Sawah Indah No. 45, Bandung",
                    phoneNumber = "08198765432",
                    paymentMethod = "E-Wallet",
                    packedDate = System.currentTimeMillis() - (1000 * 60 * 60)
                ),
                Order(
                    id = "ORD${System.currentTimeMillis() - 30000}",
                    userId = "USER001",
                    userName = "Budi Santoso",
                    items = listOf("Sprayer 16L x1"),
                    products = listOf(products[2]),
                    status = "Dikirim",
                    totalPrice = 470000.0,
                    orderDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24),
                    paymentStatus = "PAID",
                    shippingAddress = "Jl. Tani Makmur No. 123, Bogor",
                    phoneNumber = "08123456789",
                    paymentMethod = "COD",
                    packedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 20),
                    shippedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 12)
                ),
                Order(
                    id = "ORD${System.currentTimeMillis() - 40000}",
                    userId = "USER001",
                    userName = "Budi Santoso",
                    items = listOf("Selang Irigasi 50m x1"),
                    products = listOf(products[3]),
                    status = "Selesai",
                    totalPrice = 270000.0,
                    orderDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 3),
                    paymentStatus = "PAID",
                    shippingAddress = "Jl. Tani Makmur No. 123, Bogor",
                    phoneNumber = "08123456789",
                    paymentMethod = "Transfer Bank",
                    packedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 2),
                    shippedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 36),
                    completedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24)
                )
            )

            dummyOrders.forEach { OrderManager.addOrder(it) }
        }
    }

    // Dummy Reviews
    val reviews = mutableListOf(
        Review(
            id = "REV001",
            orderId = "ORD005",
            userId = "USER005",
            userName = "Joko Susilo",
            productId = "4",
            productName = "Selang Irigasi 50m",
            rating = 5f,
            comment = "Kualitas selang bagus, tidak mudah bocor. Sangat membantu untuk irigasi sawah saya!",
            reviewDate = System.currentTimeMillis() - (1000 * 60 * 60 * 20)
        ),
        Review(
            id = "REV002",
            orderId = "ORD006",
            userId = "USER006",
            userName = "Maya Sari",
            productId = "1",
            productName = "Cangkul Baja",
            rating = 5f,
            comment = "Cangkul sangat kuat dan awet. Sangat recommended untuk petani!",
            reviewDate = System.currentTimeMillis() - (1000 * 60 * 60 * 40)
        )
    )

    // Dummy Users
    private val users = mutableListOf(
        User("USER001", "Budi Santoso", "budi@email.com"),
        User("USER002", "Siti Aminah", "siti@email.com"),
        User("USER003", "Andi Wijaya", "andi@email.com"),
        User("USER004", "Rina Kartika", "rina@email.com"),
        User("USER005", "Joko Susilo", "joko@email.com"),
        User("USER006", "Maya Sari", "maya@email.com"),
        User("USER007", "Dedi Kurniawan", "dedi@email.com"),
        User("USER008", "Lina Wati", "lina@email.com"),
        User("USER009", "Rudi Hartono", "rudi@email.com"),
        User("USER010", "Dewi Lestari", "dewi@email.com")
    )

    // Dummy Notifications
    val notifications = mutableListOf(
        Notification(
            id = "NOTIF001",
            title = "🌾 Promo Alat Tani!",
            message = "Diskon hingga 50% untuk semua alat pertanian. Buruan belanja sekarang!",
            type = "BROADCAST",
            timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 2),
            recipientCount = 10
        ),
        Notification(
            id = "NOTIF002",
            title = "✨ Pupuk Baru Tersedia",
            message = "Pupuk organik premium sudah tersedia di toko kami. Cek sekarang!",
            type = "BROADCAST",
            timestamp = System.currentTimeMillis() - (1000 * 60 * 60 * 24),
            recipientCount = 10
        )
    )

    fun getAllUsers(): List<User> = users

    fun getReviewsByProduct(productId: String): List<Review> {
        return reviews.filter { it.productId == productId }
    }

    fun getAllReviews(): List<Review> {
        return reviews.sortedByDescending { it.reviewDate }
    }

    fun addReview(review: Review) {
        reviews.add(review)
    }

    fun addNotification(notification: Notification) {
        notifications.add(0, notification)
    }

    fun getCategoryById(categoryId: String): Category? {
        return categories.find { it.categoryId == categoryId }
    }

    fun getAllCategories(): List<Category> = categories
}