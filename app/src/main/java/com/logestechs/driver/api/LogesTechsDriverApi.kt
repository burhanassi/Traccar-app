package com.logestechs.driver.api

import com.logestechs.driver.api.requests.*
import com.logestechs.driver.api.responses.*
import com.logestechs.driver.data.model.Package
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

    @PUT("${AppConstants.PATH}api/driver/packages/pickup")
    suspend fun pickupPackage(@Query("barcode") barcode: String): Response<Package?>?

    @GET("${AppConstants.PATH}api/driver/customers/accepted")
    suspend fun getAcceptedPackages(
        @Query("is-grouped") isGrouped: Boolean = true,
    ): Response<GetAcceptedPackagesResponse?>?

    @GET("${AppConstants.PATH}api/driver/packages/in-car/by-villages")
    suspend fun getInCarPackagesByVillage(
        @Query("in-car-status") status: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("${AppConstants.PATH}api/driver/packages/in-car/by-customers")
    suspend fun getInCarPackagesByCustomer(
        @Query("in-car-status") status: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("${AppConstants.PATH}api/driver/packages/in-car/by-receivers")
    suspend fun getInCarPackagesByReceiver(
        @Query("in-car-status") status: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("${AppConstants.PATH}api/driver/packages/in-car/un-grouped")
    suspend fun getInCarPackagesUngrouped(
        @Query("in-car-status") status: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesUngroupedResponse?>?

    @GET("${AppConstants.PATH}api/driver/all-failure-reasons")
    suspend fun getFailureReasons(): Response<GetFailureReasonsResponse?>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/return")
    suspend fun returnPackage(
        @Path("packageId") long: Long?,
        @Body body: ReturnPackageRequestBody?
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/fail")
    suspend fun failDelivery(
        @Path("packageId") long: Long?,
        @Body body: FailDeliveryRequestBody?
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/postpone")
    suspend fun postponePackage(
        @Path("packageId") long: Long?,
        @Body body: PostponePackageRequestBody?
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/shipment-type")
    suspend fun changePackageType(
        @Path("packageId") long: Long?,
        @Body body: ChangePackageTypeRequestBody?
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/notes")
    suspend fun addPackageNote(
        @Path("packageId") long: Long?,
        @Body body: AddNoteRequestBody?
    ): Response<ResponseBody>?
}
