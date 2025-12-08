// File: data/model/ApiModels.kt
package com.example.miniproject.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

// ========== Base Response ==========
data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: Map<String, String>? = null
)

// ========== Auth Models ==========
data class LoginRequest(
    val username: String,
    val password: String
)


data class RegisterRequest(
    val username: String,

    @Json(name = "full_name")
    val fullName: String,

    val email: String,
    val phone: String,
    val password: String,

    val address: String,
    val city: String,
    val province: String,

    @Json(name = "postal_code")
    val postalCode: String
)


data class LoginResponse(
    val user: User,
    val token: String
)

// ========== User Model ==========
@Parcelize
data class User(
    val id: Int,
    val username: String? = null,
    @Json(name = "full_name")
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null,
    @Json(name = "role")
    val role: String,                   // ✅ betul
    @Json(name = "profile_image")
    val profileImage: String? = null,
    @Json(name = "created_at")
    val createdAt: String? = null
) : Parcelable



// ========== Product Models ==========
@Parcelize
data class Product(
    val id: Int,
    @Json(name = "seller_id")
    val sellerId: Int,
    @Json(name = "category_id")
    val categoryId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    @Json(name = "image_url")
    val imageUrl: String? = null,
    @Json(name = "category_name")
    val categoryName: String? = null,
    @Json(name = "seller_name")
    val sellerName: String? = null,
    val rating: Double? = 0.0,
    @Json(name = "total_reviews")
    val totalReviews: Int? = 0,
    @Json(name = "created_at")
    val createdAt: String? = null,
    @Json(name = "updated_at")
    val updatedAt: String? = null
) : Parcelable

// ========== Category Model ==========
@Parcelize
data class Category(
    val id: Int,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    @Json(name = "created_at")
    val createdAt: String? = null
) : Parcelable

// ========== Cart Models ==========
data class CartItem(
    @Json(name = "cart_id")
    val cartId: Int,
    @Json(name = "product_id")
    val productId: Int,
    @Json(name = "product_name")
    val productName: String,
    val price: Double,
    val quantity: Int,
    val stock: Int,
    @Json(name = "image_url")
    val imageUrl: String? = null,
    @Json(name = "category_name")
    val categoryName: String? = null,
    @Json(name = "seller_name")
    val sellerName: String? = null,
    val subtotal: Double,
    @Json(name = "created_at")
    val createdAt: String? = null
)

data class CartSummary(
    @Json(name = "total_items")
    val totalItems: Int,
    @Json(name = "total_quantity")
    val totalQuantity: Int,
    @Json(name = "total_price")
    val totalPrice: Double
)

data class CartResponse(
    @Json(name = "cart_items")
    val cartItems: List<CartItem>,
    val summary: CartSummary
)

data class AddToCartRequest(
    @Json(name = "product_id")
    val productId: Int,
    val quantity: Int
)

// ========== Order Models ==========
data class Order(
    val id: Int,
    @Json(name = "user_id")
    val userId: Int,
    @Json(name = "total_amount")
    val totalAmount: Double,
    val status: String, // "pending", "processing", "shipped", "delivered", "cancelled"
    @Json(name = "payment_method")
    val paymentMethod: String?,
    @Json(name = "payment_status")
    val paymentStatus: String?, // "pending", "paid", "failed"
    @Json(name = "shipping_address")
    val shippingAddress: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
)

data class OrderItem(
    val id: Int,
    @Json(name = "order_id")
    val orderId: Int,
    @Json(name = "product_id")
    val productId: Int,
    @Json(name = "product_name")
    val productName: String,
    val price: Double,
    val quantity: Int,
    val subtotal: Double
)

// ========== Review Models ==========
data class Review(
    val id: Int,
    @Json(name = "user_id")
    val userId: Int,
    @Json(name = "product_id")
    val productId: Int,
    val rating: Int,
    val comment: String?,
    @Json(name = "user_name")
    val userName: String?,
    @Json(name = "user_image")
    val userImage: String?,
    @Json(name = "created_at")
    val createdAt: String
)

data class AddReviewRequest(
    @Json(name = "product_id")
    val productId: Int,
    val rating: Int,
    val comment: String
)

// ========== Profile Models ==========

// ✅ User profile khusus endpoint /profile
data class ProfileData(
    val user: UserProfile,
    val addresses: List<UserAddress>?,
    val shop: SellerProfile?
)

// ✅ Disesuaikan dengan ProfileUser yang kamu kirim
data class UserProfile(
    val id: Int,
    val username: String? = null,
    @Json(name = "full_name")
    val fullName: String,
    val email: String,
    val phone: String? = null,
    @Json(name = "profile_image")
    val profileImage: String? = null,
    @Json(name = "role_name")
    val roleName: String? = null
)

// ✅ UserAddress sesuai kode yang kamu kirim
data class UserAddress(
    val id: Int,
    val label: String? = null,
    @Json(name = "recipient_name")
    val recipientName: String? = null,
    val phone: String? = null,
    val address: String,
    val city: String? = null,
    val province: String? = null,
    @Json(name = "postal_code")
    val postalCode: String? = null,
    @Json(name = "is_default")
    val isDefault: Int,
    @Json(name = "created_at")
    val createdAt: String? = null
)

data class SellerProfile(
    val id: Int,
    @Json(name = "shop_name")
    val shopName: String? = null,
    @Json(name = "shop_description")
    val shopDescription: String? = null,
    @Json(name = "shop_address")
    val shopAddress: String? = null
)
