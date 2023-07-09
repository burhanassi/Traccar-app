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
    @POST("api/auth/user/mobile-login")
    suspend fun login(@Body loginRequestBody: LoginRequestBody?): Response<LoginResponse?>?

    @GET("api/driver/customers/pending")
    suspend fun getPendingPackages(
        @Query("is-grouped") isGrouped: Boolean = true,
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetPendingPackagesResponse?>?

    @GET("api/driver/dashboard")
    suspend fun getDashboardInfo(
        @Query("deviceId") deviceId: Long?
    ): Response<GetDashboardInfoResponse?>?

    @PUT("api/driver/customers/{customerId}/accept")
    suspend fun acceptCustomerPackages(@Path("customerId") customerId: Long?): Response<ResponseBody?>?

    @PUT("api/driver/packages/{packageId}/accept")
    suspend fun acceptPackage(@Path("packageId") packageId: Long?): Response<ResponseBody?>?

    @PUT("api/driver/customers/{customerId}/reject")
    suspend fun rejectCustomerPackages(
        @Path("customerId") customerId: Long?,
        @Body body: RejectPackageRequestBody
    ): Response<ResponseBody?>?

    @PUT("api/driver/packages/{packageId}/reject")
    suspend fun rejectPackage(
        @Path("packageId") packageId: Long?,
        @Body body: RejectPackageRequestBody
    ): Response<ResponseBody?>?

    @PUT("api/driver/packages/pickup")
    suspend fun pickupPackage(
        @Query("barcode") barcode: String,
        @Query("is-bundle-pod-enabled") isBundlePodEnabled: Boolean? = null
    ): Response<Package?>?

    @GET("api/driver/customers/accepted")
    suspend fun getAcceptedPackages(
        @Query("is-grouped") isGrouped: Boolean = true,
    ): Response<GetAcceptedPackagesResponse?>?

    @GET("api/driver/packages/in-car/by-villages")
    suspend fun getInCarPackagesByVillage(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("api/driver/packages/in-car/by-customers")
    suspend fun getInCarPackagesByCustomer(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("api/driver/packages/in-car/by-receivers")
    suspend fun getInCarPackagesByReceiver(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("api/driver/packages/in-car/un-grouped")
    suspend fun getInCarPackagesUngrouped(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesUngroupedResponse?>?

    @GET("api/driver/packages")
    suspend fun getDeliveredPackages(
        @Query("status") status: String = "delivered",
    ): Response<GetDeliveredPackagesResponse?>?

    @GET("api/admin/customers/with-returned")
    suspend fun getCustomersWithReturnedPackages(
    ): Response<GetCustomersWithReturnedPackagesResponse?>?

    @GET("api/admin/customers/{customerId}/returned-packages")
    suspend fun getCustomerReturnedPackages(
        @Path("customerId") customerId: Long?,
        @Query("barcode") barcode: String?
    ): Response<GetCustomerReturnedPackagesResponse?>?

    @GET("api/driver/mass-packages/in-car")
    suspend fun getMassCodReports(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE
    ): Response<GetMassCodReportsResponse?>?

    @GET("api/driver/mass-packages/by-customer")
    suspend fun getMassCodReportsByCustomer(): Response<GetMassCodReportsByCustomerResponse?>?

    @PUT("api/driver/mass-packages/{reportId}/deliver")
    suspend fun deliverMassCodReport(
        @Path("reportId") packageId: Long?,
        @Body body: DeliverMassCodReportRequestBody?
    ): Response<ResponseBody>?

    @PUT("api/driver/customers/{customerId}/mass-packages/deliver")
    suspend fun deliverMassCodReportGroup(
        @Path("customerId") customerId: Long?,
        @Body body: DeliverMassCodReportGroupRequestBody?
    ): Response<ResponseBody>?

    @PUT("api/driver/customers/{customerId}/returned-packages/deliver-to-sender")
    suspend fun deliverCustomerReturnedPackagesToSender(
        @Path("customerId") customerId: Long?,
        @Body body: DeliverMassReturnedPackagesToSenderRequestBody? = null
    ): Response<ResponseBody>?

    @PUT("api/admin/packages/deliver-to-sender")
    suspend fun deliverReturnedPackageToSender(
        @Body body: DeliverReturnedPackageToSenderRequestBody?
    ): Response<ResponseBody>?

    @PUT("api/driver/packages/{packageId}/return")
    suspend fun returnPackage(
        @Path("packageId") long: Long?,
        @Body body: ReturnPackageRequestBody?
    ): Response<ResponseBody>?

    @PUT("api/driver/packages/{packageId}/fail")
    suspend fun failDelivery(
        @Path("packageId") long: Long?,
        @Body body: FailDeliveryRequestBody?
    ): Response<ResponseBody>?

    @PUT("api/driver/packages/{packageId}/postpone")
    suspend fun postponePackage(
        @Path("packageId") long: Long?,
        @Body body: PostponePackageRequestBody?
    ): Response<ResponseBody>?

    @PUT("api/driver/packages/{packageId}/shipment-type")
    suspend fun changePackageType(
        @Path("packageId") long: Long?,
        @Body body: ChangePackageTypeRequestBody?
    ): Response<ResponseBody>?

    @PUT("api/driver/packages/{packageId}/notes")
    suspend fun addPackageNote(
        @Path("packageId") long: Long?,
        @Body body: AddNoteRequestBody?
    ): Response<ResponseBody>?

    @POST("api/driver/cod/request/new")
    suspend fun codChangeRequest(
        @Body body: CodChangeRequestBody?
    ): Response<ResponseBody>?

    @Multipart
    @POST("api/driver/packages/{packageId}/signature/upload")
    suspend fun uploadPackageSignature(
        @Path("packageId") packageId: Long,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("api/driver/mass-returned-packages/signature/upload")
    suspend fun uploadMassReturnedPackagesSignature(
        @Query("customerId") customerId: Long,
        @Query("barcode") barcode: String?,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("api/driver/mass-packages/{massPackageId}/signature/upload")
    suspend fun uploadMassReportSignature(
        @Path("massPackageId") massPackageId: Long,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("api/driver/packages/{packageId}/delivery-proof/upload-multipart")
    suspend fun uploadPodImage(
        @Path("packageId") packageId: Long,
        @Query("isMultiAttachment") isMultiAttachment: Boolean? = true,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("api/driver/mass-returned-packages/delivery-proof/upload-multipart")
    suspend fun uploadMassReturnedPackagesPod(
        @Query("customerId") customerId: Long,
        @Query("barcode") barcode: String?,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("api/driver/mass-packages/{massPackageId}/delivery-proof/upload-multipart")
    suspend fun uploadPodImageForMassReport(
        @Path("massPackageId") massPackageId: Long,
        @Query("isMultiAttachment") isMultiAttachment: Boolean? = true,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @HTTP(method = "DELETE", path = "api/driver/image", hasBody = true)
    suspend fun deletePodImage(@Body body: DeleteImageRequestBody?): Response<ResponseBody?>?

    @PUT("api/driver/packages/deliver")
    suspend fun deliverPackage(
        @Query("barcode") barcode: String?,
        @Query("type") type: String?,
        @Query("note") partialDeliveryNote: String?,
        @Body body: DeliverPackageRequestBody?
    ): Response<ResponseBody>?

    @GET("api/driver/company-settings")
    suspend fun getDriverCompanySettings(): Response<GetDriverCompanySettingsResponse?>?

    @DELETE("api/auth/user/logout")
    suspend fun logout(
        @Query("workTimeTrackingId") workTimeTrackingId: Long?,
    ): Response<ResponseBody?>?

    @GET("api/driver/pickup/info")
    suspend fun getDraftPickupsCountValues(): Response<GetDraftPickupsCountValuesResponse?>?

    @GET("api/driver/pickups/with-count")
    suspend fun getDriverDraftPickups(
        @Query("status") status: String,
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetDraftPickupsResponse?>?

    @PUT("api/driver/pickups/{pickupId}/accept")
    suspend fun acceptDraftPickup(
        @Path("pickupId") long: Long?
    ): Response<ResponseBody>?

    @PUT("api/driver/pickups/{pickupId}/reject")
    suspend fun rejectDraftPickup(
        @Path("pickupId") long: Long?,
        @Query("note") status: String,
    ): Response<ResponseBody>?

    @GET("api/guests/driver/android/min-version")
    suspend fun getMinVersion(): Response<GetLatestVersionCodeResponse>?

    @GET("api/users/notifications-with-count")
    suspend fun getNotifications(
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetNotificationsResponse>?

    @POST("api/geo-services/tracking")
    suspend fun updateDriverLocation(@Body body: UpdateLocationRequestBody?): Response<ResponseBody?>?

    @PUT("api/driver/pickups/{pickupId}/scan")
    suspend fun scanDraftPickupBarcodes(
        @Path("pickupId") long: Long?,
        @Body body: ScanDraftPickupBarcodesRequestBody
    ): Response<ResponseBody>?

    @DELETE("api/driver/pickups/{pickupId}")
    suspend fun deleteDraftPickup(
        @Path("pickupId") long: Long?
    ): Response<ResponseBody>?

    @PUT("api/users/devices/reset-notification-token")
    suspend fun resetNotificationToken(@Body device: Device): Response<ResponseBody?>?

    @POST("api/customers/api-exception")
    suspend fun logException(@Body body: LogExceptionRequestBody): Response<ResponseBody>

    @PUT("api/driver/online")
    suspend fun changeWorkLogStatus(@Body body: ChangeWorkLogStatusRequestBody?): Response<ChangeWorkLogStatusResponse?>?

    @GET("api/addresses/villages")
    suspend fun getVillages(
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("search") search: String
    ): Response<GetVillagesResponse>

    @GET("api/guests/companies/info-by-domain")
    suspend fun getCompanyInfoByName(@Query("name") name: String): Response<CompanyInfo?>?

    @POST("api/companies/driver/signup")
    suspend fun signUp(
        @Body body: SignUpRequestBody?
    ): Response<ResponseBody?>?

    @GET("api/driver/packages/{packageId}/partner-name")
    suspend fun getPartnerNameByPackageId(@Path("packageId") packageId: Long): Response<GetPartnerNameResponse?>?

    @PUT("api/driver/packages/{packageId}/pickup/cancel")
    suspend fun cancelPickup(
        @Path("packageId") long: Long?,
    ): Response<ResponseBody>?

    @PUT("api/driver/shipping-plan/pickup/cancel")
    suspend fun cancelShippingPlanPickup(
        @Query("barcode") barcode: String?,
    ): Response<ResponseBody>?

    @GET("api/handler/hub/location")
    suspend fun getWarehouseLocation(@Query("barcode") barcode: String?): Response<WarehouseLocation?>?

    @PUT("api/handler/hub/locations/{locationId}/bins/sort")
    suspend fun sortBinIntoLocation(
        @Path("locationId") locationId: Long?,
        @Query("barcode") barcode: String?
    ): Response<ResponseBody?>?

    @PUT("api/handler/shipping-plan")
    suspend fun getShippingPlan(@Query("barcode") barcode: String?): Response<ShippingPlan?>?

    @GET("api/handler/hub/bin")
    suspend fun getBin(@Query("barcode") barcode: String?): Response<Bin?>?

    @PUT("api/handler/hub/bins/{binId}/shipping-items/sort")
    suspend fun sortItemIntoBin(
        @Path("binId") binId: Long?,
        @Query("shippingPlanId") shippingPlanId: Long?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoBinResponse>?

    @PUT("api/handler/locations/{locationId}/items/sort")
    suspend fun sortItemIntoLocation(
        @Path("locationId") locationId: Long?,
        @Query("shippingPlanId") shippingPlanId: Long?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoBinResponse>?

    @PUT("api/handler/hub/bins/{binId}/shipping-items/reject")
    suspend fun rejectItem(
        @Path("binId") binId: Long?,
        @Query("shippingPlanId") shippingPlanId: Long?,
        @Body body: RejectItemRequestBody?
    ): Response<RejectItemResponse>?

    @PUT("api/handler/shipping-plan/{shippingPlanId}/sorting-hours")
    suspend fun setTimeSpent(
        @Path("shippingPlanId") shippingPlanId: Long?,
        @Query("sortingHours") sortingHours: Double?
    ):Response<ResponseBody?>

    @GET("api/handler/fulfilment/orders")
    suspend fun getFulfilmentOrders(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String? = null
    ): Response<GetFulfilmentOrdersResponse?>?

    @GET("api/handler/hub/tote")
    suspend fun getTote(@Query("barcode") barcode: String?): Response<Bin?>?

    @PUT("api/handler/hub/totes/{toteId}/order-items/sort")
    suspend fun scanItemIntoTote(
        @Path("toteId") toteId: Long?,
        @Query("orderId") orderId: Long?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoToteResponse>?

    @PUT("api/handler/fulfillment-order/pack")
    suspend fun packFulfilmentOrder(
        @Query("orderId") orderId: Long?
    ): Response<ResponseBody?>?

    @PUT("api/driver/packages/{packageId}/delivery-attempt")
    suspend fun deliveryAttempt(
        @Path("packageId") packageId: Long?,
        @Query("deliveryAttemptType") deliveryAttemptType: String?
    ): Response<ResponseBody>?

    @GET("api/handler/shipping-plans")
    suspend fun getShippingPlansForHandler(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String? = null
    ): Response<GetShippingPlansResponse?>?


    @GET("api/driver/shipping-plans")
    suspend fun getShippingPlansForDriver(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
    ): Response<GetShippingPlansResponse?>?

    @PUT("api/driver/shipping-plan")
    suspend fun pickupShippingPlan(@Query("barcode") barcode: String): Response<ResponseBody?>?

    @GET("api/driver/shipping-plans/stats")
    suspend fun getDriverShippingPlansCountValues(): Response<GetDriverShippingPlansCountValuesResponse?>?

    @PUT("api/driver/sub-bundles/pickup")
    suspend fun pickupBundle(
        @Query("packageId") packageId: Long?,
        @Body body: PickupBundleRequestBody?
    ): Response<ResponseBody?>?

    @POST("api/driver/packages/{packageId}/pin-code")
    suspend fun requestPinCodeSms(
        @Path("packageId") packageId: Long?
    ): Response<ResponseBody?>?

    @PUT("api/driver/packages/{packageId}/pin-code")
    suspend fun verifyDeliveryPin(
        @Path("packageId") packageId: Long?,
        @Query("pinCode") pinCode: String?
    ): Response<ResponseBody?>?

    @GET("api/driver/destination-locations")
    suspend fun getDriverPackagesLocations(
        @Query("latStart") lat: Double?,
        @Query("longStart") lng: Double?
    ): Response<GetDriverPackagesLocationsResponse?>?
}
