package com.example.miniproject.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.miniproject.model.Category
import com.example.miniproject.model.Product

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "miniproject.db"
        private const val DATABASE_VERSION = 1

        // Table Categories
        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_DESCRIPTION = "description"
        const val COL_CAT_ICON = "icon"
        const val COL_CAT_CREATED_AT = "created_at"

        // Table Products
        const val TABLE_PRODUCTS = "products"
        const val COL_PROD_ID = "id"
        const val COL_PROD_NAME = "name"
        const val COL_PROD_PRICE = "price"
        const val COL_PROD_DESCRIPTION = "description"
        const val COL_PROD_IMAGE = "image_url"
        const val COL_PROD_CATEGORY_ID = "category_id"
        const val COL_PROD_STOCK = "stock"
        const val COL_PROD_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create Categories Table
        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CAT_NAME TEXT NOT NULL,
                $COL_CAT_DESCRIPTION TEXT,
                $COL_CAT_ICON TEXT,
                $COL_CAT_CREATED_AT TEXT NOT NULL
            )
        """.trimIndent()

        // Create Products Table
        val createProductsTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COL_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PROD_NAME TEXT NOT NULL,
                $COL_PROD_PRICE REAL NOT NULL,
                $COL_PROD_DESCRIPTION TEXT,
                $COL_PROD_IMAGE TEXT,
                $COL_PROD_CATEGORY_ID INTEGER,
                $COL_PROD_STOCK INTEGER NOT NULL,
                $COL_PROD_CREATED_AT TEXT NOT NULL,
                FOREIGN KEY($COL_PROD_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID)
            )
        """.trimIndent()

        db?.execSQL(createCategoriesTable)
        db?.execSQL(createProductsTable)

        // Insert dummy categories
        insertDummyCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    private fun insertDummyCategories(db: SQLiteDatabase?) {
        val dummyCategories = listOf(
            "Peralatan",
            "Pupuk",
            "Benih"
        )

        dummyCategories.forEach { categoryName ->
            val values = ContentValues().apply {
                put(COL_CAT_NAME, categoryName)
                put(COL_CAT_DESCRIPTION, "Kategori $categoryName")
                put(COL_CAT_ICON, "ic_category")
                put(COL_CAT_CREATED_AT, getCurrentDateTime())
            }
            db?.insert(TABLE_CATEGORIES, null, values)
        }
    }

    // ==================== CATEGORIES CRUD ====================

    fun addCategory(name: String, description: String = "", icon: String = "ic_category"): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CAT_NAME, name)
            put(COL_CAT_DESCRIPTION, description)
            put(COL_CAT_ICON, icon)
            put(COL_CAT_CREATED_AT, getCurrentDateTime())
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES ORDER BY $COL_CAT_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME))
                val createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_CREATED_AT))

                categories.add(Category(id, name, createdAt, null))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }

    fun updateCategory(id: Int, name: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CAT_NAME, name)
        }
        return db.update(TABLE_CATEGORIES, values, "$COL_CAT_ID = ?", arrayOf(id.toString()))
    }

    fun deleteCategory(id: Int): Int {
        val db = writableDatabase
        // Hapus produk yang terkait kategori ini juga
        db.delete(TABLE_PRODUCTS, "$COL_PROD_CATEGORY_ID = ?", arrayOf(id.toString()))
        return db.delete(TABLE_CATEGORIES, "$COL_CAT_ID = ?", arrayOf(id.toString()))
    }

    fun isCategoryNameExists(name: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CATEGORIES WHERE $COL_CAT_NAME = ?",
            arrayOf(name)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // ==================== PRODUCTS CRUD ====================

    fun addProduct(
        name: String,
        price: Double,
        description: String,
        imageUrl: String?,
        categoryId: Int,
        stock: Int
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PROD_NAME, name)
            put(COL_PROD_PRICE, price)
            put(COL_PROD_DESCRIPTION, description)
            put(COL_PROD_IMAGE, imageUrl)
            put(COL_PROD_CATEGORY_ID, categoryId)
            put(COL_PROD_STOCK, stock)
            put(COL_PROD_CREATED_AT, getCurrentDateTime())
        }
        return db.insert(TABLE_PRODUCTS, null, values)
    }

    fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase

        val query = """
            SELECT p.*, c.$COL_CAT_NAME as category_name
            FROM $TABLE_PRODUCTS p
            LEFT JOIN $TABLE_CATEGORIES c ON p.$COL_PROD_CATEGORY_ID = c.$COL_CAT_ID
            ORDER BY p.$COL_PROD_CREATED_AT DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PROD_PRICE))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_DESCRIPTION))
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_IMAGE))
                val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_CATEGORY_ID))
                val stock = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_STOCK))
                val createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_CREATED_AT))
                val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))

                products.add(
                    Product(
                        id = id,
                        name = name,
                        price = price,
                        description = description,
                        imageUrl = imageUrl,
                        categoryId = categoryId,
                        stock = stock,
                        categoryName = categoryName,
                        createdAt = createdAt,
                        imageResId = null
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }

    fun getProductsByCategory(categoryId: Int): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase

        val query = """
            SELECT p.*, c.$COL_CAT_NAME as category_name
            FROM $TABLE_PRODUCTS p
            LEFT JOIN $TABLE_CATEGORIES c ON p.$COL_PROD_CATEGORY_ID = c.$COL_CAT_ID
            WHERE p.$COL_PROD_CATEGORY_ID = ?
            ORDER BY p.$COL_PROD_CREATED_AT DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(categoryId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_NAME))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PROD_PRICE))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_DESCRIPTION))
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_IMAGE))
                val stock = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_STOCK))
                val createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_CREATED_AT))
                val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))

                products.add(
                    Product(
                        id = id,
                        name = name,
                        price = price,
                        description = description,
                        imageUrl = imageUrl,
                        categoryId = categoryId,
                        stock = stock,
                        categoryName = categoryName,
                        createdAt = createdAt,
                        imageResId = null
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }

    fun updateProduct(
        id: Int,
        name: String,
        price: Double,
        description: String,
        imageUrl: String?,
        categoryId: Int,
        stock: Int
    ): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PROD_NAME, name)
            put(COL_PROD_PRICE, price)
            put(COL_PROD_DESCRIPTION, description)
            put(COL_PROD_IMAGE, imageUrl)
            put(COL_PROD_CATEGORY_ID, categoryId)
            put(COL_PROD_STOCK, stock)
        }
        return db.update(TABLE_PRODUCTS, values, "$COL_PROD_ID = ?", arrayOf(id.toString()))
    }

    fun deleteProduct(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_PRODUCTS, "$COL_PROD_ID = ?", arrayOf(id.toString()))
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}