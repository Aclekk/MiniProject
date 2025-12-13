package com.example.miniproject.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

// =====================
// Base Response
// =====================
data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: Map<String, String>? = null
)

// =====================
// AUTH
// =====================
data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    @Json(name = "full_name") val fullName: String,
    val email: String,
    val phone: String,
    val password: String,
    val address: String,
    val city: String,
    val province: String,
    @Json(name = "postal_code") val postalCode: String
)

data class LoginResponse(
    val user: User,
    val token: String
)

@Parcelize
data class User(
    val id: Int,
    val username: String? = null,
    @Json(name = "full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null,
    @Json(name = "role") val role: String,
    @Json(name = "profile_image") val profileImage: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
) : Parcelable

// =====================
// ✅ CHECKOUT REQUEST (sesuai validasi backend / Postman kamu)
// Endpoint: orders/create.php
// =====================
data class CheckoutRequest(
    @Json(name = "product_id") val product_id: Int,
    val quantity: Int,

    @Json(name = "shipping_address") val shipping_address: String,
    @Json(name = "shipping_city") val shipping_city: String,
    @Json(name = "shipping_province") val shipping_province: String,
    @Json(name = "shipping_postal_code") val shipping_postal_code: String,

    @Json(name = "recipient_name") val recipient_name: String,
    @Json(name = "recipient_phone") val recipient_phone: String,

    @Json(name = "shipping_cost") val shipping_cost: Double,
    val note: String? = null,

    @Json(name = "payment_method") val payment_method: String
)

// =====================
// PRODUCTS
// =====================
@Parcelize
data class Product(
    val id: Int,
    @Json(name = "seller_id") val sellerId: Int,
    @Json(name = "category_id") val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "category_name") val categoryName: String? = null,
    @Json(name = "seller_name") val sellerName: String? = null,
    val rating: Double? = 0.0,
    @Json(name = "total_reviews") val totalReviews: Int? = 0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
) : Parcelable

@Parcelize
data class Category(
    val id: Int,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
) : Parcelable

// =====================
// CART
// =====================
data class CartItem(
    @Json(name = "cart_id") val cartId: Int,
    @Json(name = "product_id") val productId: Int,
    @Json(name = "product_name") val productName: String,
    val price: Double,
    val quantity: Int,
    val stock: Int,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "category_name") val categoryName: String? = null,
    @Json(name = "seller_name") val sellerName: String? = null,
    val subtotal: Double,
    @Json(name = "created_at") val createdAt: String? = null
)

data class CartSummary(
    @Json(name = "total_items") val totalItems: Int,
    @Json(name = "total_quantity") val totalQuantity: Int,
    @Json(name = "total_price") val totalPrice: Double
)

data class CartResponse(
    @Json(name = "cart_items") val cartItems: List<CartItem>,
    val summary: CartSummary
)

data class AddToCartRequest(
    @Json(name = "product_id") val productId: Int,
    val quantity: Int
)

// =====================
// ORDERS
// =====================
// ✅ Sesuai response Postman kamu:
// data: { order_id, order_number, buyer_id, seller_id, status, payment_method, subtotal, shipping_cost, total_amount }
data class CreateOrderResult(
    @Json(name = "order_id") val orderId: Int? = null,
    @Json(name = "order_number") val orderNumber: String? = null,
    @Json(name = "buyer_id") val buyerId: Int? = null,
    @Json(name = "seller_id") val sellerId: Int? = null,
    val status: String? = null,
    @Json(name = "payment_method") val paymentMethod: String? = null,
    val subtotal: Double? = null,
    @Json(name = "shipping_cost") val shippingCost: Double? = null,
    @Json(name = "total_amount") val totalAmount: Double? = null
)

data class OrderStatusResponse(
    @Json(name = "order_id") val orderId: Int,
    val status: String
)

// ✅ SellerOrderResponse tetap di sini
data class SellerOrderResponse(
    val id: Int,
    @Json(name = "order_number") val orderNumber: String? = null,
    @Json(name = "buyer_id") val buyerId: Int? = null,
    @Json(name = "buyer_name") val buyerName: String? = null,
    @Json(name = "product_id") val productId: Int? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_price") val productPrice: Double? = null,
    @Json(name = "product_image") val productImage: String? = null,
    val quantity: Int? = null,
    @Json(name = "total_amount") val totalAmount: Double? = null,
    val status: String? = null,
    @Json(name = "payment_method") val paymentMethod: String? = null,
    @Json(name = "shipping_address") val shippingAddress: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

// =====================
// PROFILE (/profile/me.php)
// =====================
data class ProfileData(
    val user: UserProfile,
    val addresses: List<UserAddress>?,
    val shop: SellerProfile?
)

data class UserProfile(
    val id: Int,
    val username: String? = null,
    @Json(name = "full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    @Json(name = "profile_image") val profileImage: String? = null,
    @Json(name = "role_name") val roleName: String? = null
)

data class UserAddress(
    val id: Int,
    val label: String? = null,
    @Json(name = "recipient_name") val recipientName: String? = null,
    val phone: String? = null,
    val address: String,
    val city: String? = null,
    val province: String? = null,
    @Json(name = "postal_code") val postalCode: String? = null,
    @Json(name = "is_default") val isDefault: Int,
    @Json(name = "created_at") val createdAt: String? = null
)

data class SellerProfile(
    val id: Int,
    @Json(name = "shop_name") val shopName: String? = null,
    @Json(name = "shop_description") val shopDescription: String? = null,
    @Json(name = "shop_address") val shopAddress: String? = null
)

// =====================
// Review API model (biar ResponseModels.kt aman)
// =====================
data class ReviewApi(
    val id: Int,
    @Json(name = "order_id") val orderId: Int,
    @Json(name = "product_id") val productId: Int,
    @Json(name = "user_id") val userId: Int,
    val rating: Int,
    val comment: String?,
    @Json(name = "is_anonymous") val isAnonymous: Int,
    @Json(name = "created_at") val createdAt: String?,

    // extra dari query join backend
    @Json(name = "product_name") val productName: String?,
    @Json(name = "product_image") val productImage: String?,
    @Json(name = "order_number") val orderNumber: String?,

    // khusus seller list (join users)
    @Json(name = "user_name") val userName: String?,
    @Json(name = "user_image") val userImage: String?
)

data class AddReviewRequest(
    @Json(name = "order_id") val orderId: Int,
    @Json(name = "product_id") val productId: Int,
    val rating: Int,
    val comment: String,
    @Json(name = "is_anonymous") val isAnonymous: Int = 0
)

