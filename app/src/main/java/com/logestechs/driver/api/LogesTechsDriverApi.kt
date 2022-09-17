package com.logestechs.driver.api

import com.logestechs.driver.api.requests.*
import com.logestechs.driver.api.responses.*
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.utils.AppConstants
import okhttp3.MultipartBody
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

    @GET("${AppConstants.PATH}api/driver/packages")
    suspend fun getDeliveredPackages(
        @Query("status") status: String = "delivered",
    ): Response<GetDeliveredPackagesResponse?>?

    @GET("${AppConstants.PATH}api/admin/customers/with-returned")
    suspend fun getCustomersWithReturnedPackages(
    ): Response<GetCustomersWithReturnedPackagesResponse?>?

    @GET("${AppConstants.PATH}api/admin/customers/{customerId}/returned-packages")
    suspend fun getCustomerReturnedPackages(
        @Path("customerId") customerId: Long?,
    ): Response<GetCustomerReturnedPackagesResponse?>?

    @GET("${AppConstants.PATH}api/driver/mass-packages/in-car")
    suspend fun getMassCodReports(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE
    ): Response<GetMassCodReportsResponse?>?

    @PUT("${AppConstants.PATH}api/driver/mass-packages/{reportId}/deliver")
    suspend fun deliverMassCodReport(
        @Path("reportId") packageId: Long?
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/driver/customers/{customerId}/returned-packages/deliver-to-sender")
    suspend fun deliverCustomerReturnedPackagesToSender(
        @Path("customerId") customerId: Long?
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/admin/packages/deliver-to-sender")
    suspend fun deliverReturnedPackageToSender(
        @Body body: DeliverReturnedPackageToSenderRequestBody?
    ): Response<ResponseBody>?

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

    @POST("${AppConstants.PATH}api/driver/cod/request/new")
    suspend fun codChangeRequest(
        @Body body: CodChangeRequestBody?
    ): Response<ResponseBody>?

    @Multipart
    @POST("${AppConstants.PATH}api/driver/packages/{packageId}/signature/upload")
    suspend fun uploadPackageSignature(
        @Path("packageId") packageId: Long,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("${AppConstants.PATH}api/driver/packages/{packageId}/delivery-proof/upload-multipart")
    suspend fun uploadPodImage(
        @Path("packageId") packageId: Long,
        @Query("isMultiAttachment") isMultiAttachment: Boolean? = true,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @HTTP(method = "DELETE", path = "${AppConstants.PATH}api/driver/image", hasBody = true)
    suspend fun deletePodImage(@Body body: DeleteImageRequestBody?): Response<ResponseBody?>?

    @PUT("${AppConstants.PATH}api/driver/packages/deliver")
    suspend fun deliverPackage(
        @Query("barcode") barcode: String?,
        @Query("type") type: String?,
        @Query("note") partialDeliveryNote: String?,
        @Body body: DeliverPackageRequestBody?
    ): Response<ResponseBody>?

    @GET("${AppConstants.PATH}api/driver/company-settings")
    suspend fun getDriverCompanySettings(): Response<GetDriverCompanySettingsResponse?>?

    @DELETE("${AppConstants.PATH}api/auth/user/logout")
    suspend fun logout(): Response<ResponseBody?>?

    @GET("${AppConstants.PATH}api/driver/pickup/info")
    suspend fun getDraftPickupsCountValues(): Response<GetDraftPickupsCountValuesResponse?>?

    @GET("${AppConstants.PATH}api/driver/pickups/with-count")
    suspend fun getDriverDraftPickups(
        @Query("status") status: String,
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetDraftPickupsResponse?>?

    @PUT("${AppConstants.PATH}api/driver/pickups/{pickupId}/accept")
    suspend fun acceptDraftPickup(
        @Path("pickupId") long: Long?
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/driver/pickups/{pickupId}/reject")
    suspend fun rejectDraftPickup(
        @Path("pickupId") long: Long?,
        @Query("note") status: String,
    ): Response<ResponseBody>?

    @GET("${AppConstants.PATH}api/guests/driver/ios/min-version")
    suspend fun getMinVersion(): Response<GetLatestVersionCodeResponse>?
}
