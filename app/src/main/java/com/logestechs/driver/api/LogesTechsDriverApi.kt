package com.logestechs.driver.api

import com.logestechs.driver.api.requests.LoginRequestBody
import com.logestechs.driver.api.requests.RejectPackageRequestBody
import com.logestechs.driver.api.responses.GetDashboardInfoResponse
import com.logestechs.driver.api.responses.GetPendingPackagesResponse
import com.logestechs.driver.api.responses.LoginResponse
import com.logestechs.driver.utils.AppConstants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

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

    @PUT("${AppConstants.PATH}api/driver/customers/{customerId}/accept")
    suspend fun acceptCustomerPackages(@Path("customerId") customerId: Long?): Response<ResponseBody?>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/accept")
    suspend fun acceptPackage(@Path("packageId") packageId: Long?): Response<ResponseBody?>?

    @PUT("${AppConstants.PATH}api/driver/customers/{customerId}/reject")
    suspend fun rejectCustomerPackages(
        @Path("customerId") customerId: Long?,
        @Body body: RejectPackageRequestBody
    ): Response<ResponseBody?>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/reject")
    suspend fun rejectPackage(
        @Path("packageId") packageId: Long?,
        @Body body: RejectPackageRequestBody
    ): Response<ResponseBody?>?
}

