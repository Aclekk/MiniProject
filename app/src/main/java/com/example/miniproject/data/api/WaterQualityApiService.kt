package com.example.miniproject.data.api

import com.example.miniproject.data.model.WaterQualityResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WaterQualityApiService {

    @Multipart
    @POST("predict")
    suspend fun predictWaterQuality(
        @Part image: MultipartBody.Part
    ): Response<WaterQualityResponse>
}
