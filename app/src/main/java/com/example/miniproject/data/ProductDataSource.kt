package com.example.miniproject.data

import com.example.miniproject.R
import com.example.miniproject.model.Product

object ProductDataSource {

    private val _products = mutableListOf<Product>()

    // 🟢 Ambil semua produk
    fun getAllProducts(): MutableList<Product> = _products

    // 🟢 Ambil produk berdasarkan kategori
    fun getProductsByCategory(categoryName: String): List<Product> {
        return _products.filter { it.categoryName.equals(categoryName, ignoreCase = true) }
    }

    // 🟢 Update produk (misal edit dari admin)
    fun updateProduct(updatedProduct: Product) {
        val index = _products.indexOfFirst { it.id == updatedProduct.id }
        if (index != -1) _products[index] = updatedProduct
    }

    // 🟢 Hapus produk
    fun deleteProduct(product: Product) {
        _products.removeAll { it.id == product.id }
    }

    // 🟢 Load dummy data (dipanggil sekali saat app start)
    fun loadDummyData() {
        if (_products.isNotEmpty()) return // biar ga dobel load

        _products.addAll(
            listOf(
                Product(
                    id = 1,
                    name = "Cangkul Premium",
                    price = 150000.0,
                    description = "Cangkul baja berkualitas tinggi untuk mengolah tanah",
                    imageUrl = null,
                    imageResId = R.drawable.cangkul,
                    categoryId = 1,
                    stock = 10,
                    categoryName = "Peralatan",
                    createdAt = "2025-01-01"
                ),
                Product(
                    id = 2,
                    name = "Pupuk Organik 25kg",
                    price = 200000.0,
                    description = "Pupuk alami ramah lingkungan berkualitas premium",
                    imageUrl = null,
                    imageResId = R.drawable.pupuk,
                    categoryId = 2,
                    stock = 30,
                    categoryName = "Pupuk",
                    createdAt = "2025-01-01"
                ),
                Product(
                    id = 3,
                    name = "Benih Padi Premium",
                    price = 50000.0,
                    description = "Benih unggul padi hasil seleksi terbaik",
                    imageUrl = null,
                    imageResId = R.drawable.benih,
                    categoryId = 3,
                    stock = 100,
                    categoryName = "Benih",
                    createdAt = "2025-01-01"
                ),
                Product(
                    id = 4,
                    name = "Traktor Mini",
                    price = 5000000.0,
                    description = "Traktor pertanian mini untuk lahan kecil dan menengah",
                    imageUrl = null,
                    imageResId = R.drawable.traktor,
                    categoryId = 4,
                    stock = 5,
                    categoryName = "Alat Pertanian",
                    createdAt = "2025-01-01"
                ),
                Product(
                    id = 5,
                    name = "Rotavator Tangan",
                    price = 950000.0,
                    description = "Alat putar penggembur tanah manual atau elektrik",
                    imageUrl = null,
                    imageResId = R.drawable.rotavator,
                    categoryId = 1,
                    stock = 7,
                    categoryName = "Peralatan",
                    createdAt = "2025-01-02"
                ),
                Product(
                    id = 6,
                    name = "Sekop Kebun",
                    price = 85000.0,
                    description = "Sekop multifungsi untuk menggali dan memindahkan material",
                    imageUrl = null,
                    imageResId = R.drawable.sekop,
                    categoryId = 1,
                    stock = 35,
                    categoryName = "Peralatan",
                    createdAt = "2025-01-02"
                ),
                Product(
                    id = 7,
                    name = "Selang Irigasi 20m",
                    price = 120000.0,
                    description = "Selang elastis kualitas premium untuk sistem penyiraman",
                    imageUrl = null,
                    imageResId = R.drawable.selang,
                    categoryId = 1,
                    stock = 40,
                    categoryName = "Peralatan",
                    createdAt = "2025-01-02"
                ),
                Product(
                    id = 8,
                    name = "Arit Tajam",
                    price = 55000.0,
                    description = "Arit berbahan baja untuk memotong rumput dan panen padi",
                    imageUrl = null,
                    imageResId = R.drawable.arit,
                    categoryId = 1,
                    stock = 20,
                    categoryName = "Peralatan",
                    createdAt = "2025-01-02"
                ),
                Product(
                    id = 9,
                    name = "Sprayer Elektrik 16L",
                    price = 450000.0,
                    description = "Alat semprot hama otomatis dengan baterai lithium tahan lama",
                    imageUrl = null,
                    imageResId = R.drawable.siraman,
                    categoryId = 1,
                    stock = 15,
                    categoryName = "Peralatan",
                    createdAt = "2025-01-03"
                ),
                Product(
                    id = 10,
                    name = "Pupuk NPK Mutiara",
                    price = 95000.0,
                    description = "Pupuk lengkap untuk semua jenis tanaman dengan kandungan seimbang",
                    imageUrl = null,
                    imageResId = R.drawable.benihihi,
                    categoryId = 2,
                    stock = 50,
                    categoryName = "Pupuk",
                    createdAt = "2025-01-03"
                ),
                Product(
                    id = 11,
                    name = "Benih Jagung Hibrida",
                    price = 75000.0,
                    description = "Benih jagung varietas unggul tahan hama dengan hasil maksimal",
                    imageUrl = null,
                    imageResId = R.drawable.jagung,
                    categoryId = 3,
                    stock = 80,
                    categoryName = "Benih",
                    createdAt = "2025-01-03"
                ),
                Product(
                    id = 12,
                    name = "Mesin Pemotong Rumput",
                    price = 1250000.0,
                    description = "Mesin potong rumput profesional dengan mesin bensin 2 tak",
                    imageUrl = null,
                    imageResId = R.drawable.motong,
                    categoryId = 1,
                    stock = 8,
                    categoryName = "Peralatan",
                    createdAt = "2025-01-04"
                ),
                Product(
                    id = 13,
                    name = "Pestisida Nabati Organik",
                    price = 65000.0,
                    description = "Pestisida alami berbahan dasar tumbuhan aman untuk lingkungan",
                    imageUrl = null,
                    imageResId = R.drawable.petis,
                    categoryId = 5,
                    stock = 60,
                    categoryName = "Pestisida",
                    createdAt = "2025-01-04"
                )
            )
        )

        // 🆕 Load dummy reviews
        loadDummyReviews()
    }

    // 🟢 Load dummy review
    private fun loadDummyReviews() {
        val dummyReviews = listOf(
            Review(999, 1, 5f, "Cangkulnya mantap! Kualitas baja bagus dan tahan lama 👍", "Budi Petani", "2025-01-10"),
            Review(998, 1, 4.5f, "Bagus sih tapi agak berat, tapi hasil kerja memuaskan", "Siti Maryam", "2025-01-12"),
            Review(997, 2, 5f, "Pupuknya organik asli! Tanaman saya jadi subur banget 🌱", "Ahmad Tani", "2025-01-08"),
            Review(996, 3, 4f, "Benih berkualitas, tingkat tumbuh tinggi. Recommended!", "Dewi Sartika", "2025-01-09"),
            Review(995, 4, 5f, "Traktornya kuat banget 💪", "Raden Agus", "2025-01-13"),
            Review(994, 5, 4.5f, "Rotavator-nya ringan dan praktis", "Lia Kusuma", "2025-01-14"),
            Review(993, 6, 4f, "Sekopnya solid dan tahan karat", "Eka Pratama", "2025-01-14"),
            Review(992, 7, 4.5f, "Selangnya lentur banget, cocok buat kebun", "Roni Suherman", "2025-01-15"),
            Review(991, 8, 5f, "Aritnya tajam banget, hati-hati 😅", "Putri Anggun", "2025-01-15"),
            Review(990, 9, 5f, "Sprayer elektriknya keren! Hemat tenaga dan efisien ⚡", "Joko Widodo", "2025-01-05"),
            Review(989, 10, 4.5f, "NPK mutiaranya bagus, tanaman cabai berbuah lebat 🌶️", "Rina Susanti", "2025-01-06"),
            Review(988, 11, 5f, "Jagungnya tumbuh sempurna! Panen melimpah 🌽", "Eko Prabowo", "2025-01-07"),
            Review(987, 12, 4f, "Mesin potong rumputnya powerful, tapi agak berisik", "Linda Wijaya", "2025-01-11"),
            Review(986, 13, 5f, "Pestisidanya aman banget buat tanaman saya 🌿", "Teguh Ramadhan", "2025-01-16")
        )

        dummyReviews.forEach { CartManager.addReview(it) }
    }
}