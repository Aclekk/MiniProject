package com.example.miniproject.data.api

import com.example.miniproject.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== AUTH ==========
    @POST("api/auth/register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<BaseResponse<ProfileData>>

    @POST("api/auth/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

    // ========== PROFILE ==========
    @GET("profile/me.php")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<BaseResponse<ProfileData>>

    @Multipart
    @POST("profile/update.php")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Part("full_name") fullName: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part("province") province: RequestBody?,
        @Part("postal_code") postalCode: RequestBody?,
        @Part("recipient_name") recipientName: RequestBody?,
        @Part("recipient_phone") recipientPhone: RequestBody?,
        @Part profileImage: MultipartBody.Part?
    ): Response<BaseResponse<ProfileData>>

    // ========== ADDRESS ==========
    @POST("address/add.php")
    suspend fun addAddress(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Response<BaseResponse<UserAddress>>

    @PUT("address/update.php")
    suspend fun updateAddress(
        @Header("Authorization") token: String,
        @Query("id") addressId: Int,
        @Body request: Map<String, Any>
    ): Response<BaseResponse<UserAddress>>

    // ========== PRODUCTS ==========
    @GET("products/list.php")
    suspend fun getProducts(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20,
        @Query("category_id") categoryId: Int? = null,
        @Query("search") search: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null
    ): Response<BaseResponse<ProductListResponse>>

    @GET("products/detail.php")
    suspend fun getProductDetail(
        @Query("id") productId: Int
    ): Response<BaseResponse<Product>>

    @Multipart
    @POST("products/create.php")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Part("name") name: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponse<Product>>

    // ✅ UPDATE PRODUCT
    @Multipart
    @POST("products/update.php")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Part("product_id") productId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("description") description: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponse<Product>>

    // ✅ DELETE PRODUCT
    @FormUrlEncoded
    @POST("products/delete.php")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Field("id") productId: Int
    ): Response<BaseResponse<Any>>

    // Produk terlaris (Home / Products)
    @GET("products/best_sellers.php")
    suspend fun getBestSellerProducts(
    ): Response<BaseResponse<ProductListResponse>>

    @FormUrlEncoded
    @POST("products/toggle_best_seller.php")
    suspend fun toggleBestSeller(
        @Header("Authorization") token: String,
        @Field("product_id") productId: Int,
        @Field("is_best_seller") isBestSeller: Int
    ): Response<BaseResponse<Any>>

    // ========== CATEGORIES ==========
    @GET("categories/list.php")
    suspend fun getCategories(): Response<BaseResponse<CategoriesData>>

    @FormUrlEncoded
    @POST("categories/add.php")
    suspend fun addCategory(
        @Field("name") name: String,
        @Field("description") description: String?
    ): Response<BaseResponse<Any>>

    @FormUrlEncoded
    @POST("categories/update.php")
    suspend fun updateCategory(
        @Field("id") id: Int,
        @Field("name") name: String,
        @Field("description") description: String?
    ): Response<BaseResponse<Any>>

    @FormUrlEncoded
    @POST("categories/delete.php")
    suspend fun deleteCategory(
        @Field("id") id: Int
    ): Response<BaseResponse<Any>>


    // ========== CART ==========
    @GET("cart/list.php")
    suspend fun getCart(
        @Header("Authorization") token: String
    ): Response<BaseResponse<CartResponse>>

    @POST("api/cart/add.php")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Body request: AddToCartRequest
    ): Response<BaseResponse<Any>>

    @DELETE("cart/remove.php")
    suspend fun removeFromCart(
        @Header("Authorization") token: String,
        @Query("id") cartId: Int
    ): Response<BaseResponse<Any>>

    @PUT("cart/update.php")
    suspend fun updateCartQuantity(
        @Header("Authorization") token: String,
        @Body request: Map<String, Int>
    ): Response<BaseResponse<Any>>

    // ========== ORDERS ==========
    @POST("orders/create.php")
    suspend fun checkout(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Response<BaseResponse<Order>>

    @GET("orders/list.php")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): Response<BaseResponse<OrdersData>>

    @GET("orders/detail.php")
    suspend fun getOrderDetail(
        @Header("Authorization") token: String,
        @Query("id") orderId: Int
    ): Response<BaseResponse<Order>>

    @PUT("orders/update_status.php")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Response<BaseResponse<Any>>

    // ========== REVIEWS ==========
    @GET("reviews/list.php")
    suspend fun getProductReviews(
        @Query("product_id") productId: Int
    ): Response<BaseResponse<ReviewsData>>

    @POST("reviews/create.php")
    suspend fun addReview(
        @Header("Authorization") token: String,
        @Body request: AddReviewRequest
    ): Response<BaseResponse<Review>>

    // ========== PAYMENTS ==========
    @Multipart
    @POST("payment/upload_proof.php")
    suspend fun uploadPaymentProof(
        @Header("Authorization") token: String,
        @Part("order_id") orderId: RequestBody,
        @Part("payment_method") paymentMethod: RequestBody,
        @Part("notes") notes: RequestBody?,
        @Part payment_proof: MultipartBody.Part
    ): Response<BaseResponse<Any>>

    // ========== WISHLIST ==========
    @POST("wishlist/add.php")
    suspend fun addToWishlist(
        @Header("Authorization") token: String,
        @Body request: Map<String, Int>
    ): Response<BaseResponse<Any>>

    @GET("wishlist/list.php")
    suspend fun getWishlist(
        @Header("Authorization") token: String
    ): Response<BaseResponse<WishlistData>>

    @DELETE("wishlist/remove.php")
    suspend fun removeFromWishlist(
        @Header("Authorization") token: String,
        @Query("id") wishlistId: Int
    ): Response<BaseResponse<Any>>

    // ========== SELLER DASHBOARD ==========
    @GET("api/seller/dashboard.php")
    suspend fun getSellerDashboard(
        @Header("Authorization") token: String
    ): Response<BaseResponse<DashboardData>>

    @GET("api/seller/sales_report.php")
    suspend fun getSalesReport(
        @Header("Authorization") token: String,
        @Query("period") period: String?,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): Response<BaseResponse<SalesReportData>>

    // ========== NOTIFICATIONS ==========
    @GET("api/notifications/list.php")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("page") page: Int?,
        @Query("limit") limit: Int?,
        @Query("is_read") isRead: Int?
    ): Response<BaseResponse<NotificationsData>>

    @PUT("api/notifications/mark_read.php")
    suspend fun markNotificationRead(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Response<BaseResponse<Any>>
}
