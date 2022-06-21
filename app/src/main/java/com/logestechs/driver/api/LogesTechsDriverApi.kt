package com.logestechs.driver.api

import com.logestechs.driver.api.requests.LoginRequestBody
import com.logestechs.driver.api.responses.GetDashboardInfoResponse
import com.logestechs.driver.api.responses.GetPendingPackagesResponse
import com.logestechs.driver.api.responses.LoginResponse
import com.logestechs.driver.utils.AppConstants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LogesTechsDriverApi {
    @POST("${AppConstants.PATH}api/auth/user/mobile-login")
    suspend fun login(@Body loginRequestBody: LoginRequestBody?): Response<LoginResponse?>?

    @GET("${AppConstants.PATH}api/driver/customers/pending")
    suspend fun getPendingPackages(
        @Query("is-grouped") isGrouped: Boolean = true,
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetPendingPackagesResponse?>?

    @GET("${AppConstants.PATH}api/driver/dashboard")
    suspend fun getDashboardInfo(): Response<GetDashboardInfoResponse?>?
}

