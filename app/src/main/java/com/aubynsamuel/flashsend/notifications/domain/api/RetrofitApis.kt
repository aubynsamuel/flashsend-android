package com.aubynsamuel.flashsend.notifications.domain.api

import com.aubynsamuel.flashsend.notifications.model.ApiResponse
import com.aubynsamuel.flashsend.notifications.model.HealthResponse
import com.aubynsamuel.flashsend.notifications.model.MarkAsReadRequest
import com.aubynsamuel.flashsend.notifications.model.ReplyRequest
import com.aubynsamuel.flashsend.notifications.model.SendNotificationRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/api/sendNotification")
    suspend fun sendNotification(
        @Body request: SendNotificationRequest
    ): ApiResponse

    @POST("/api/reply")
    suspend fun sendReply(
        @Body request: ReplyRequest
    ): ApiResponse

    @POST("/api/markAsRead")
    suspend fun markMessagesAsRead(
        @Body request: MarkAsReadRequest
    ): ApiResponse

    @GET("/health")
    suspend fun getHealthStatus(): HealthResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://chat-server-xet3.onrender.com"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}