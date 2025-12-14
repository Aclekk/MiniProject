package com.example.miniproject.data.model

import com.google.gson.annotations.SerializedName

// ============================================
// 📦 CATEGORIES DATA
// ============================================
data class CategoriesData(
    @SerializedName("categories") val categories: List<Category>
)

// ============================================
// 📦 ORDERS DATA
// ============================================
data class OrdersData(
    @SerializedName("orders") val orders: List<Order>,
    @SerializedName("pagination") val pagination: Pagination?
)

// ✅ TAMBAHAN: Order model untuk kebutuhan ResponseModels & beberapa endpoint
// Ini TIDAK mengubah Order lokal kamu (com.example.miniproject.data.Order)
data class Order(
    @SerializedName("id") val id: Int,
    @SerializedName("order_number") val orderNumber: String? = null,
    @SerializedName("buyer_id") val buyerId: Int? = null,
    @SerializedName("seller_id") val sellerId: Int? = null,
    @SerializedName("subtotal") val subtotal: Double? = null,
    @SerializedName("shipping_cost") val shippingCost: Double? = null,
    @SerializedName("discount_amount") val discountAmount: Double? = null,
    @SerializedName("total_amount") val totalAmount: Double? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("shipping_address") val shippingAddress: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)
// ============================================
// 📦 REVIEWS DATA
// ============================================
data class ReviewsData(
    @SerializedName("reviews") val reviews: List<ReviewApi>,
    @SerializedName("rating_summary") val ratingSummary: RatingSummary?,
    @SerializedName("pagination") val pagination: Pagination?
)

data class RatingSummary(
    @SerializedName("average_rating") val averageRating: Float?,
    @SerializedName("total_reviews") val totalReviews: Int?,
    @SerializedName("rating_breakdown") val ratingBreakdown: List<RatingBreakdown>?
)

data class RatingBreakdown(
    @SerializedName("rating") val rating: Int,
    @SerializedName("count") val count: Int
)

// ============================================
// 📦 WISHLIST DATA
// ============================================
data class WishlistData(
    @SerializedName("wishlist") val wishlist: List<WishlistItem>
)

data class WishlistItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("product_id") val productId: Int?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("stock") val stock: Int?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("shop_name") val shopName: String?,
    @SerializedName("product_image") val productImage: String?,
    @SerializedName("average_rating") val averageRating: Float?,
    @SerializedName("is_available") val isAvailable: Boolean?,
    @SerializedName("created_at") val createdAt: String?
)

// ============================================
// 📦 DASHBOARD DATA (SELLER)
// ============================================
data class DashboardData(
    @SerializedName("products") val products: ProductStats?,
    @SerializedName("orders") val orders: OrderStats?,
    @SerializedName("revenue") val revenue: RevenueStats?,
    @SerializedName("top_products") val topProducts: List<TopProduct>?,
    @SerializedName("recent_orders") val recentOrders: List<Order>?,
    @SerializedName("low_stock_products") val lowStockProducts: List<Product>?
)

data class ProductStats(
    @SerializedName("total") val total: Int,
    @SerializedName("active") val active: Int,
    @SerializedName("out_of_stock") val outOfStock: Int
)

data class OrderStats(
    @SerializedName("total") val total: Int,
    @SerializedName("pending") val pending: Int,
    @SerializedName("processing") val processing: Int,
    @SerializedName("completed") val completed: Int
)

data class RevenueStats(
    @SerializedName("total") val total: Double,
    @SerializedName("today") val today: Double,
    @SerializedName("week") val week: Double,
    @SerializedName("month") val month: Double
)

data class TopProduct(
    @SerializedName("id") val id: Int?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("sold_count") val soldCount: Int?,
    @SerializedName("total_sold") val totalSold: Int?,
    @SerializedName("total_revenue") val totalRevenue: Double?,
    @SerializedName("image") val image: String?
)

// ============================================
// 📦 SALES REPORT DATA
// ============================================

data class DateRange(
    @SerializedName("start") val start: String?,
    @SerializedName("end") val end: String?
)

data class SalesSummary(
    @SerializedName("total_orders") val totalOrders: Int,
    @SerializedName("total_items_sold") val totalItemsSold: Int,
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("average_order_value") val averageOrderValue: Double
)

data class DailySale(
    @SerializedName("date") val date: String?,
    @SerializedName("orders") val orders: Int,
    @SerializedName("items_sold") val itemsSold: Int,
    @SerializedName("revenue") val revenue: Double
)

data class ProductSale(
    @SerializedName("id") val id: Int?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("quantity_sold") val quantitySold: Int?,
    @SerializedName("revenue") val revenue: Double?,
    @SerializedName("image") val image: String?
)

data class SalesByStatus(
    @SerializedName("order_status") val orderStatus: String?,
    @SerializedName("count") val count: Int,
    @SerializedName("revenue") val revenue: Double
)

// ============================================
// 📦 NOTIFICATIONS DATA
// ============================================
data class NotificationsData(
    @SerializedName("notifications") val notifications: List<Notification>,
    @SerializedName("unread_count") val unreadCount: Int,
    @SerializedName("pagination") val pagination: Pagination?
)

data class Notification(
    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("is_read") val isRead: Boolean?,
    @SerializedName("read_at") val readAt: String?,
    @SerializedName("created_at") val createdAt: String?
)

// ============================================
// 📦 PAGINATION (SHARED)
// ============================================
data class Pagination(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("total_pages") val totalPages: Int
)
