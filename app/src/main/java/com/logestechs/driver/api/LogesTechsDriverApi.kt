package com.logestechs.driver.api

import com.logestechs.driver.api.requests.*
import com.logestechs.driver.api.responses.*
import com.logestechs.driver.data.model.*
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
    suspend fun getDashboardInfo(
        @Query("deviceId") deviceId: Long?
    ): Response<GetDashboardInfoResponse?>?

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
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("${AppConstants.PATH}api/driver/packages/in-car/by-customers")
    suspend fun getInCarPackagesByCustomer(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("${AppConstants.PATH}api/driver/packages/in-car/by-receivers")
    suspend fun getInCarPackagesByReceiver(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("${AppConstants.PATH}api/driver/packages/in-car/un-grouped")
    suspend fun getInCarPackagesUngrouped(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
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
        @Path("reportId") packageId: Long?,
        @Body body: DeliverMassCodReportRequestBody?
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
    @POST("${AppConstants.PATH}api/driver/mass-packages/{massPackageId}/signature/upload")
    suspend fun uploadMassReportSignature(
        @Path("massPackageId") massPackageId: Long,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("${AppConstants.PATH}api/driver/packages/{packageId}/delivery-proof/upload-multipart")
    suspend fun uploadPodImage(
        @Path("packageId") packageId: Long,
        @Query("isMultiAttachment") isMultiAttachment: Boolean? = true,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("${AppConstants.PATH}api/driver/mass-packages/{massPackageId}/delivery-proof/upload-multipart")
    suspend fun uploadPodImageForMassReport(
        @Path("massPackageId") massPackageId: Long,
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
    suspend fun logout(
        @Query("workTimeTrackingId") workTimeTrackingId: Long?,
    ): Response<ResponseBody?>?

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

    @GET("${AppConstants.PATH}api/guests/driver/android/min-version")
    suspend fun getMinVersion(): Response<GetLatestVersionCodeResponse>?

    @GET("${AppConstants.PATH}api/users/notifications-with-count")
    suspend fun getNotifications(
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetNotificationsResponse>?

    @POST("${AppConstants.PATH}api/geo-services/tracking")
    suspend fun updateDriverLocation(@Body body: UpdateLocationRequestBody?): Response<ResponseBody?>?

    @PUT("${AppConstants.PATH}api/driver/pickups/{pickupId}/scan")
    suspend fun scanDraftPickupBarcodes(
        @Path("pickupId") long: Long?,
        @Body body: ScanDraftPickupBarcodesRequestBody
    ): Response<ResponseBody>?

    @DELETE("${AppConstants.PATH}api/driver/pickups/{pickupId}")
    suspend fun deleteDraftPickup(
        @Path("pickupId") long: Long?
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/users/devices/reset-notification-token")
    suspend fun resetNotificationToken(@Body device: Device): Response<ResponseBody?>?

    @POST("${AppConstants.PATH}api/customers/api-exception")
    suspend fun logException(@Body body: LogExceptionRequestBody): Response<ResponseBody>

    @PUT("${AppConstants.PATH}api/driver/online")
    suspend fun changeWorkLogStatus(@Body body: ChangeWorkLogStatusRequestBody?): Response<ChangeWorkLogStatusResponse?>?

    @GET("${AppConstants.PATH}api/addresses/villages")
    suspend fun getVillages(
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("search") search: String
    ): Response<GetVillagesResponse>

    @GET("${AppConstants.PATH}api/guests/companies/info-by-domain")
    suspend fun getCompanyInfoByName(@Query("name") name: String): Response<CompanyInfo?>?

    @POST("${AppConstants.PATH}api/companies/driver/signup")
    suspend fun signUp(
        @Body body: SignUpRequestBody?
    ): Response<ResponseBody?>?

    @GET("${AppConstants.PATH}api/driver/packages/{packageId}/partner-name")
    suspend fun getPartnerNameByPackageId(@Path("packageId") packageId: Long): Response<GetPartnerNameResponse?>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/pickup/cancel")
    suspend fun cancelPickup(
        @Path("packageId") long: Long?,
    ): Response<ResponseBody>?

    @PUT("${AppConstants.PATH}api/driver/shipping-plan/pickup/cancel")
    suspend fun cancelShippingPlanPickup(
        @Query("barcode") barcode: String?,
    ): Response<ResponseBody>?

    @GET("${AppConstants.PATH}api/handler/hub/location")
    suspend fun getWarehouseLocation(@Query("barcode") barcode: String?): Response<WarehouseLocation?>?

    @PUT("${AppConstants.PATH}api/handler/hub/locations/{locationId}/bins/sort")
    suspend fun sortBinIntoLocation(
        @Path("locationId") locationId: Long?,
        @Query("barcode") barcode: String?
    ): Response<ResponseBody?>?

    @PUT("${AppConstants.PATH}api/handler/shipping-plan")
    suspend fun getShippingPlan(@Query("barcode") barcode: String?): Response<ShippingPlan?>?

    @GET("${AppConstants.PATH}api/handler/hub/bin")
    suspend fun getBin(@Query("barcode") barcode: String?): Response<Bin?>?

    @PUT("${AppConstants.PATH}api/handler/hub/bins/{binId}/shipping-items/sort")
    suspend fun sortItemIntoBin(
        @Path("binId") binId: Long?,
        @Query("shippingPlanId") shippingPlanId: Long?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoBinResponse>?

    @PUT("${AppConstants.PATH}api/handler/hub/bins/{binId}/shipping-items/reject")
    suspend fun rejectItem(
        @Path("binId") binId: Long?,
        @Query("shippingPlanId") shippingPlanId: Long?,
        @Body body: BarcodeRequestBody?
    ): Response<RejectShippingPlanItemResponse?>?

    @GET("${AppConstants.PATH}api/handler/fulfilment/orders")
    suspend fun getFulfilmentOrders(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String? = null
    ): Response<GetFulfilmentOrdersResponse?>?

    @GET("${AppConstants.PATH}api/handler/hub/tote")
    suspend fun getTote(@Query("barcode") barcode: String?): Response<Bin?>?

    @PUT("${AppConstants.PATH}api/handler/hub/totes/{toteId}/order-items/sort")
    suspend fun scanItemIntoTote(
        @Path("toteId") toteId: Long?,
        @Query("orderId") orderId: Long?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoToteResponse>?

    @PUT("${AppConstants.PATH}api/handler/fulfillment-order/pack")
    suspend fun packFulfilmentOrder(
        @Query("orderId") orderId: Long?
    ): Response<ResponseBody?>?

    @PUT("${AppConstants.PATH}api/driver/packages/{packageId}/delivery-attempt")
    suspend fun deliveryAttempt(
        @Path("packageId") packageId: Long?,
        @Query("deliveryAttemptType") deliveryAttemptType: String?
    ): Response<ResponseBody>?

    @GET("${AppConstants.PATH}api/handler/shipping-plans")
    suspend fun getShippingPlansForHandler(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String? = null
    ): Response<GetShippingPlansResponse?>?


    @GET("${AppConstants.PATH}api/driver/shipping-plans")
    suspend fun getShippingPlansForDriver(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
    ): Response<GetShippingPlansResponse?>?

    @PUT("${AppConstants.PATH}api/driver/shipping-plan")
    suspend fun pickupShippingPlan(@Query("barcode") barcode: String): Response<ResponseBody?>?

    @GET("${AppConstants.PATH}api/driver/shipping-plans/stats")
    suspend fun getDriverShippingPlansCountValues(): Response<GetDriverShippingPlansCountValuesResponse?>?
}
