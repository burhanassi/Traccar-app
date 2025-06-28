package com.logestechs.traccarApp.api

import com.logestechs.traccarApp.api.requests.*
import com.logestechs.traccarApp.api.responses.*
import com.logestechs.traccarApp.data.model.*
import com.logestechs.traccarApp.utils.AppConstants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TraccarAppApi {
    @FormUrlEncoded
    @POST("session")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    @POST("devices")
    @Headers("Content-Type: application/json")
    suspend fun register(@Body device: DeviceRequest): Response<DeviceResponse>

    @POST("geo-services/tracking")
    suspend fun updateDriverLocation(@Body body: UpdateLocationRequestBody?): Response<ResponseBody?>?

    @GET("users/notifications-with-count")
    suspend fun getNotifications(
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetNotificationsResponse>?
}
