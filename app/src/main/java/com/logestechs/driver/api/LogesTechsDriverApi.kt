package com.logestechs.driver.api

import com.logestechs.driver.api.requests.*
import com.logestechs.driver.api.responses.*
import com.logestechs.driver.data.model.*
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.PaymentGatewayType
import com.logestechs.driver.utils.ReturnedPackageStatus
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface LogesTechsDriverApi {
    @POST("auth/user/mobile-login")
    suspend fun login(@Body loginRequestBody: LoginRequestBody?): Response<LoginResponse?>?

    @GET("driver/customers/pending")
    suspend fun getPendingPackages(
        @Query("is-grouped") isGrouped: Boolean = true,
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetPendingPackagesResponse?>?

    @GET("driver/dashboard")
    suspend fun getDashboardInfo(
        @Query("deviceId") deviceId: Long?
    ): Response<GetDashboardInfoResponse?>?

    @PUT("driver/customers/{customerId}/accept")
    suspend fun acceptCustomerPackages(@Path("customerId") customerId: Long?): Response<ResponseBody?>?

    @PUT("driver/packages/{packageId}/accept")
    suspend fun acceptPackage(@Path("packageId") packageId: Long?): Response<ResponseBody?>?

    @PUT("driver/customers/{customerId}/reject")
    suspend fun rejectCustomerPackages(
        @Path("customerId") customerId: Long?,
        @Body body: RejectPackageRequestBody
    ): Response<ResponseBody?>?

    @PUT("driver/packages/{packageId}/reject")
    suspend fun rejectPackage(
        @Path("packageId") packageId: Long?,
        @Body body: RejectPackageRequestBody
    ): Response<ResponseBody?>?

    @PUT("driver/packages/pickup")
    suspend fun pickupPackage(
        @Query("barcode") barcode: String,
        @Query("is-bundle-pod-enabled") isBundlePodEnabled: Boolean? = null,
        @Query("isToFinalDestination") isToFinalDestination: Boolean? = null
    ): Response<Package?>?

    @GET("driver/customers/accepted")
    suspend fun getAcceptedPackages(
        @Query("is-grouped") isGrouped: Boolean = true,
    ): Response<GetAcceptedPackagesResponse?>?

    @GET("driver/customers/{customerId}/packages/accepted")
    suspend fun getAcceptedPackagesByCustomer(
        @Path("customerId") customerId: Long?,
    ): Response<List<Package>?>?

    @GET("driver/packages/in-car/by-villages")
    suspend fun getInCarPackagesByVillage(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("driver/packages/in-car/by-customers")
    suspend fun getInCarPackagesByCustomer(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("driver/packages/in-car/by-receivers")
    suspend fun getInCarPackagesByReceiver(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesGroupedResponse?>?

    @GET("driver/packages/in-car/un-grouped")
    suspend fun getInCarPackagesUngrouped(
        @Query("in-car-status") status: String?,
        @Query("shipment-type") packageType: String?,
        @Query("search") search: String? = null
    ): Response<GetInCarPackagesUngroupedResponse?>?

    @GET("driver/packages")
    suspend fun getDeliveredPackages(
        @Query("status") status: String = "delivered",
    ): Response<GetDeliveredPackagesResponse?>?

    @GET("admin/customers/with-returned")
    suspend fun getCustomersWithReturnedPackages(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("type") type: ReturnedPackageStatus?,
        @Query("isToDeliverToSender") isToDeliverToSender: Boolean?
    ): Response<GetCustomersWithReturnedPackagesResponse?>?

    @GET("admin/customers/{customerId}/returned-packages")
    suspend fun getCustomerReturnedPackages(
        @Path("customerId") customerId: Long?,
        @Query("barcode") barcode: String?,
        @Query("type") type: ReturnedPackageStatus?,
        @Query("isToDeliverToSender") isToDeliverToSender: Boolean?
    ): Response<GetCustomerReturnedPackagesResponse?>?

    @GET("admin/bundles/returned")
    suspend fun getCustomersWithReturnedBundles(): Response<GetCustomersWithReturnedBundlesResponse?>?

    @GET("admin/bundles/{bundleId}/packages")
    suspend fun getCustomerReturnedBundles(
        @Path("bundleId") customerId: Long?
    ): Response<GetCustomerReturnedPackagesResponse?>?

    @GET("driver/mass-packages/in-car")
    suspend fun getMassCodReports(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE
    ): Response<GetMassCodReportsResponse?>?

    @GET("driver/mass-packages/by-customer")
    suspend fun getMassCodReportsByCustomer(): Response<GetMassCodReportsByCustomerResponse?>?

    @PUT("driver/mass-packages/{reportId}/deliver")
    suspend fun deliverMassCodReport(
        @Path("reportId") packageId: Long?,
        @Body body: DeliverMassCodReportRequestBody?
    ): Response<ResponseBody>?

    @PUT("driver/customers/{customerId}/mass-packages/deliver")
    suspend fun deliverMassCodReportGroup(
        @Path("customerId") customerId: Long?,
        @Body body: DeliverMassCodReportGroupRequestBody?
    ): Response<ResponseBody>?

    @PUT("driver/customers/{customerId}/returned-packages/deliver-to-sender")
    suspend fun deliverCustomerReturnedPackagesToSender(
        @Path("customerId") customerId: Long?,
        @Body body: DeliverMassReturnedPackagesToSenderRequestBody? = null
    ): Response<ResponseBody>?

    @PUT("driver/returned-bundles/{bundleId}/deliver-to-sender")
    suspend fun deliverCustomerReturnedBundlesToSender(
        @Path("bundleId") bundleId: Long?,
        @Body body: DeliverMassReturnedPackagesToSenderRequestBody? = null
    ): Response<ResponseBody>?

    @PUT("admin/packages/deliver-to-sender")
    suspend fun deliverReturnedPackageToSender(
        @Body body: DeliverReturnedPackageToSenderRequestBody?
    ): Response<ResponseBody>?

    @PUT("driver/packages/{packageId}/return")
    suspend fun returnPackage(
        @Path("packageId") long: Long?,
        @Body body: ReturnPackageRequestBody?
    ): Response<ResponseBody>?

    @GET("driver/packages/{packageId}/attachments")
    suspend fun packageAttachments(
        @Path("packageId") long: Long?
    ): Response<List<String>>?

    @PUT("driver/packages/{packageId}/fail")
    suspend fun failDelivery(
        @Path("packageId") long: Long?,
        @Body body: FailDeliveryRequestBody?
    ): Response<ResponseBody>?

    @PUT("driver/packages/{packageId}/postpone")
    suspend fun postponePackage(
        @Path("packageId") long: Long?,
        @Body body: PostponePackageRequestBody?
    ): Response<ResponseBody>?

    @PUT("driver/packages/{packageId}/shipment-type")
    suspend fun changePackageType(
        @Path("packageId") long: Long?,
        @Body body: ChangePackageTypeRequestBody?
    ): Response<ResponseBody>?

    @PUT("driver/packages/{packageId}/notes")
    suspend fun addPackageNote(
        @Path("packageId") long: Long?,
        @Body body: AddNoteRequestBody?
    ): Response<ResponseBody>?

    @POST("driver/cod/request/new")
    suspend fun codChangeRequest(
        @Body body: CodChangeRequestBody?
    ): Response<ResponseBody>?

    @Multipart
    @POST("driver/packages/{packageId}/signature/upload")
    suspend fun uploadPackageSignature(
        @Path("packageId") packageId: Long,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("driver/mass-returned-packages/signature/upload")
    suspend fun uploadMassReturnedPackagesSignature(
        @Query("customerId") customerId: Long,
        @Query("barcode") barcode: String?,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("driver/mass-packages/{massPackageId}/signature/upload")
    suspend fun uploadMassReportSignature(
        @Path("massPackageId") massPackageId: Long,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("driver/packages/{packageId}/delivery-proof/upload-multipart")
    suspend fun uploadPodImage(
        @Path("packageId") packageId: Long,
        @Query("isMultiAttachment") isMultiAttachment: Boolean? = true,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("handler/item/image/upload")
    suspend fun uploadPodImageForRejectedItem(
        @Query("barcode") barcode: String,
        @Part file: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("driver/mass-returned-packages/delivery-proof/upload-multipart")
    suspend fun uploadMassReturnedPackagesPod(
        @Query("customerId") customerId: Long,
        @Query("barcode") barcode: String?,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("driver/mass-packages/{massPackageId}/delivery-proof/upload-multipart")
    suspend fun uploadPodImageForMassReport(
        @Path("massPackageId") massPackageId: Long,
        @Query("isMultiAttachment") isMultiAttachment: Boolean? = true,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @Multipart
    @POST("driver/customers/{customerId}/mass-packages/delivery-proof/upload-multipart")
    suspend fun uploadPodGroupImageForMassReport(
        @Path("customerId") massPackageId: Long,
        @Part upload_form: MultipartBody.Part?
    ): Response<UploadImageResponse?>?

    @HTTP(method = "DELETE", path = "driver/image", hasBody = true)
    suspend fun deletePodImage(@Body body: DeleteImageRequestBody?): Response<ResponseBody?>?

    @PUT("driver/packages/deliver")
    suspend fun deliverPackage(
        @Query("barcode") barcode: String?,
        @Query("type") type: String?,
        @Query("note") partialDeliveryNote: String?,
        @Body body: DeliverPackageRequestBody?
    ): Response<ResponseBody>?

    @GET("driver/company-settings")
    suspend fun getDriverCompanySettings(): Response<GetDriverCompanySettingsResponse?>?

    @DELETE("auth/user/logout")
    suspend fun logout(
        @Query("workTimeTrackingId") workTimeTrackingId: Long?,
    ): Response<ResponseBody?>?

    @GET("driver/pickup/info")
    suspend fun getDraftPickupsCountValues(): Response<GetDraftPickupsCountValuesResponse?>?

    @GET("driver/pickups/with-count")
    suspend fun getDriverDraftPickups(
        @Query("status") status: String,
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetDraftPickupsResponse?>?

    @PUT("driver/pickups/{pickupId}/accept")
    suspend fun acceptDraftPickup(
        @Path("pickupId") long: Long?
    ): Response<ResponseBody>?

    @PUT("driver/pickups/{pickupId}/reject")
    suspend fun rejectDraftPickup(
        @Path("pickupId") long: Long?,
        @Query("note") status: String,
    ): Response<ResponseBody>?

    @GET("guests/driver/android/min-version")
    suspend fun getMinVersion(): Response<GetLatestVersionCodeResponse>?

    @GET("users/notifications-with-count")
    suspend fun getNotifications(
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<GetNotificationsResponse>?

    @PUT("users/notifications/set-as-read")
    suspend fun setAllNotificationAsRead(): Response<ResponseBody>?
    @PUT("users/notifications/{notificationId}/set-as-read")
    suspend fun setNotificationRead(
        @Path("notificationId") notificationId: Long?
    ): Response<ResponseBody>?

    @POST("geo-services/tracking")
    suspend fun updateDriverLocation(@Body body: UpdateLocationRequestBody?): Response<ResponseBody?>?

    @PUT("driver/pickups/{pickupId}/scan")
    suspend fun scanDraftPickupBarcodes(
        @Path("pickupId") long: Long?,
        @Body body: ScanDraftPickupBarcodesRequestBody
    ): Response<ResponseBody>?

    @DELETE("driver/pickups/{pickupId}")
    suspend fun deleteDraftPickup(
        @Path("pickupId") long: Long?
    ): Response<ResponseBody>?

    @PUT("users/devices/reset-notification-token")
    suspend fun resetNotificationToken(@Body device: Device): Response<ResponseBody?>?

    @POST("customers/api-exception")
    suspend fun logException(@Body body: LogExceptionRequestBody): Response<ResponseBody>

    @PUT("driver/online")
    suspend fun changeWorkLogStatus(@Body body: ChangeWorkLogStatusRequestBody?): Response<ChangeWorkLogStatusResponse?>?

    @GET("addresses/villages")
    suspend fun getVillages(
        @Query("pageSize") pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("search") search: String
    ): Response<GetVillagesResponse>

    @GET("guests/companies/info-by-domain")
    suspend fun getCompanyInfoByName(@Query("name") name: String): Response<CompanyInfo?>?

    @POST("companies/driver/signup")
    suspend fun signUp(
        @Body body: SignUpRequestBody?
    ): Response<ResponseBody?>?

    @GET("driver/packages/{packageId}/partner-name")
    suspend fun getPartnerNameByPackageId(@Path("packageId") packageId: Long): Response<GetPartnerNameResponse?>?

    @PUT("driver/packages/{packageId}/pickup/cancel")
    suspend fun cancelPickup(
        @Path("packageId") long: Long?,
    ): Response<ResponseBody>?

    @PUT("driver/shipping-plan/pickup/cancel")
    suspend fun cancelShippingPlanPickup(
        @Query("barcode") barcode: String?,
    ): Response<ResponseBody>?

    @GET("handler/hub/location")
    suspend fun getWarehouseLocation(@Query("barcode") barcode: String?): Response<WarehouseLocation?>?

    @GET("handler/item/detail")
    suspend fun searchForInventoryItem(@Query("barcode") barcode: String?): Response<InventoryItemResponse?>?

    @GET("handler/damaged-location")
    suspend fun getWarehouseDamagedLocation(
        @Query("barcode") barcode: String?,
        @Query("shippingPlanId") shippingPlanId: Long?,
    ): Response<WarehouseLocation?>?

    @PUT("handler/hub/locations/{locationId}/bins/sort")
    suspend fun sortBinIntoLocation(
        @Path("locationId") locationId: Long?,
        @Query("barcode") barcode: String?
    ): Response<ResponseBody?>?

    @PUT("handler/shipping-plan")
    suspend fun getShippingPlan(@Query("barcode") barcode: String?): Response<ShippingPlan?>?

    @GET("handler/hub/bin")
    suspend fun getBin(@Query("barcode") barcode: String?): Response<Bin?>?

    @PUT("handler/hub/bins/{binId}/shipping-items/sort")
    suspend fun sortItemIntoBin(
        @Path("binId") binId: Long?,
        @Query("shippingPlanId") shippingPlanId: Long?,
        @Query("quantity") quantity: Int?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoBinResponse>?

    @PUT("handler/locations/{locationId}/items/sort")
    suspend fun sortItemIntoLocation(
        @Path("locationId") locationId: Long?,
        @Query("shippingPlanId") shippingPlanId: Long?,
        @Query("quantity") quantity: Int?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoBinResponse>?

    @PUT("handler/locations/{locationId}/items/rejected/sort")
    suspend fun sortRejectedItemIntoLocation(
        @Path("locationId") locationId: Long?,
        @Query("itemBarcode") itemBarcode: String
    ): Response<SortRejectedItemIntoBinResponse>?

    @PUT("handler/hub/shipping-items/reject")
    suspend fun rejectItem(
        @Query("binId") binId: Long?,
        @Query("locationId") locationId: Long?,
        @Query("shippingPlanId") shippingPlanId: Long?,
        @Body body: RejectItemRequestBody?
    ): Response<RejectItemResponse>?

    @PUT("handler/shipping-plan/{shippingPlanId}/sorting-hours")
    suspend fun setTimeSpent(
        @Path("shippingPlanId") shippingPlanId: Long?,
        @Query("sortingHours") sortingHours: Double?
    ):Response<ResponseBody?>

    @GET("handler/fulfilment/orders")
    suspend fun getFulfilmentOrders(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String?,
        @Query("statuses") statuses: List<String>? = null
    ): Response<GetFulfilmentOrdersResponse?>?

    @GET("handler/hub/tote")
    suspend fun getTote(
        @Query("barcode") barcode: String?,
        @Query("orderId") orderId: Long?
    ): Response<Bin?>?

    @PUT("handler/hub/totes/{toteId}/order-items/sort")
    suspend fun scanItemIntoTote(
        @Path("toteId") toteId: Long?,
        @Query("orderId") orderId: Long?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoToteResponse>?

    @PUT("handler/order-items/pick")
    suspend fun scanItemsIntoTote(
        @Query("orderIds") orderIds: List<Long?>,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoToteResponse>?

    @PUT("handler/tote/order/{orderId}/sort")
    suspend fun scanOrderIntoTote(
        @Path("orderId") orderId: Long?,
        @Query("barcode") barcode: String?
    ): Response<ResponseBody>?

    @PUT("handler/hub/order-items/continue-picking")
    suspend fun continuePicking(
        @Query("orderId") orderId: Long?,
        @Body body: BarcodeRequestBody?
    ): Response<SortItemIntoToteResponse>?

    @PUT("handler/fulfillment-order/pack")
    suspend fun packFulfilmentOrder(
        @Query("orderId") orderId: Long?,
        @Query("isPackedOrderWithoutScanningItems") isPackedOrderWithoutScanningItems: Boolean = true
    ): Response<ResponseBody?>?

    @GET("handler/order/{orderId}/tote")
    suspend fun scanToteToPack(
        @Path("orderId") orderId: Long?,
        @Query("barcode") barcode: String?
    ): Response<ResponseBody?>?

    @GET("handler/orders/{orderId}/picked-item")
    suspend fun packFulfilmentOrderByItem(
        @Path("orderId") orderId: Long?,
        @Query("barcode") barcode: String?
    ): Response<ProductItem?>?

    @PUT("driver/packages/{packageId}/delivery-attempt")
    suspend fun deliveryAttempt(
        @Path("packageId") packageId: Long?,
        @Query("deliveryAttemptType") deliveryAttemptType: String?
    ): Response<ResponseBody>?

    @GET("handler/shipping-plans")
    suspend fun getShippingPlansForHandler(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String? = null
    ): Response<GetShippingPlansResponse?>?


    @GET("driver/shipping-plans")
    suspend fun getShippingPlansForDriver(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
    ): Response<GetShippingPlansResponse?>?

    @PUT("driver/shipping-plan")
    suspend fun pickupShippingPlan(@Query("barcode") barcode: String): Response<ResponseBody?>?

    @GET("driver/shipping-plans/stats")
    suspend fun getDriverShippingPlansCountValues(): Response<GetDriverShippingPlansCountValuesResponse?>?

    @PUT("driver/sub-bundles/pickup")
    suspend fun pickupBundle(
        @Query("packageId") packageId: Long?,
        @Body body: PickupBundleRequestBody?
    ): Response<ResponseBody?>?

    @POST("driver/packages/{packageId}/pin-code")
    suspend fun requestPinCodeSms(
        @Path("packageId") packageId: Long?
    ): Response<ResponseBody?>?

    @POST("driver/packages/{packageId}/clickpay")
    suspend fun requestPaymentLinkClickPay(
        @Path("packageId") packageId: Long?
    ): Response<ResponseBody?>?

    @GET("driver/packages/{packageId}/is-e-paid")
    suspend fun verifyClickPay(
        @Path("packageId") packageId: Long?
    ): Response<VerifyClickPayResponse?>?

    @POST("driver/packages/{packageId}/payment-gateway/pay")
    suspend fun paymentGateway(
        @Path("packageId") packageId: Long?,
        @Query("reference") reference: String?,
        @Query("type") type: PaymentGatewayType?
    ): Response<ResponseBody?>?

    @POST("driver/bundles/{bundleId}/pin-code")
    suspend fun requestPinCodeSmsForBundles(
        @Path("bundleId") bundleId: Long?
    ): Response<ResponseBody?>?

    @POST("driver/mass-cod-package/{massPkgId}/pin-code")
    suspend fun requestPinCodeSmsForMassCod(
        @Path("massPkgId") massPkgId: Long?
    ): Response<ResponseBody?>?

    @POST("driver/mass-returned/{massPkgBarcode}/pin-code")
    suspend fun requestPinCodeSmsForReturned(
        @Path("massPkgBarcode") massPkgBarcode: String?
    ): Response<ResponseBody?>?

    @PUT("driver/packages/{packageId}/pin-code")
    suspend fun verifyDeliveryPin(
        @Path("packageId") packageId: Long?,
        @Query("pinCode") pinCode: String?
    ): Response<ResponseBody?>?

    @PUT("driver/bundles/{bundleId}/pin-code")
    suspend fun verifyDeliveryPinForBundles(
        @Path("bundleId") bundleId: Long?,
        @Query("pinCode") pinCode: String?
    ): Response<ResponseBody?>?

    @PUT("driver/mass-cod-package/{massPkgId}/pin-code")
    suspend fun verifyDeliveryPinForMassCod(
        @Path("massPkgId") massPkgId: Long?,
        @Query("pinCode") pinCode: String?
    ): Response<ResponseBody?>?

    @PUT("driver/mass-returned/{massPkgBarcode}/pin-code")
    suspend fun verifyDeliveryPinForReturned(
        @Path("massPkgBarcode") massPkgBarcode: String?,
        @Query("pinCode") pinCode: String?
    ): Response<ResponseBody?>?

    @GET("driver/destination-locations")
    suspend fun getDriverPackagesLocations(
        @Query("latStart") lat: Double?,
        @Query("longStart") lng: Double?
    ): Response<GetDriverPackagesLocationsResponse?>?

    @PUT("driver/packages/route/order")
    suspend fun sendDriverRoute(
        @Body body: DriverRouteRequestBody?
    ): Response<ResponseBody?>?

    @GET("driver/checkins")
    suspend fun getCheckIns(
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
    ): Response<ArrayList<CheckIns>?>?

    @GET("driver/hubs/scan")
    suspend fun scanHub(@Query("barcode") barcode: String): Response<Hub>?

    @POST("driver/checkin")
    suspend fun checkIn(@Body checkIn: CheckIns): Response<ResponseBody>

    @GET("driver/packages/{packageId}")
    suspend fun trackPackageDriverNotification(
        @Path("packageId") packageId: Long
    ): Response<Package>?

    @GET("handler/shelves/scan")
    suspend fun scanShelfByBarcode(
        @Query("barcode") barcode: String?
    ): Response<Shelf?>

    @GET("handler/shelves/{shelfId}/packages/sort")
    suspend fun scanPackagesOnShelf(
        @Path("shelfId") shelfId: Long?,
        @Query("barcode") barcode: String?
    ): Response<GetPackageOnShelfResponse?>?

    @PUT("handler/packages/scan-to-unload")
    suspend fun unloadPackageFromCustomer(
        @Query("barcode") barcode: String?
    ): Response<Package?>?

    @GET("handler/packages/{barcode}")
    suspend fun findPackage(
        @Path("barcode") barcode: String?
    ): Response<Package?>?

    @GET("handler/drivers/verify")
    suspend fun verifyDriver(
        @Query("barcode") barcode: String?,
        @Query("pageSize") pageSize: Int? = AppConstants.DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = AppConstants.DEFAULT_PAGE,
        @Query("search") search: String? = null,
    ): Response<GetVerfiyDriverResponse?>?

    @PUT("handler/packages/scan-to-unload")
    suspend fun unloadPackageFromContainerToHub(
        @Query("barcode") barcode: String?,
        @Query("driverId") driverId: Long?
    ): Response<Package>

    @PUT("handler/packages/{packageId}/flag")
    suspend fun flagPackageInShelf(@Path("packageId") packageId: Long): Response<ResponseBody>

    @PUT("handler/packages/{packageId}/un-flag")
    suspend fun unFlagPackageInShelf(@Path("packageId") packageId: Long): Response<ResponseBody>

    @GET("driver/customer/{customerId}/accepted/pdf-report")
    suspend fun printPackageAwb(
        @Path("customerId") id: Long,
        @Query("is-image") isImage: Boolean,
        @Query("timezone") timezone: String? = TimeZone.getDefault().id.toString(),
    ): Response<PrintAwbResponse>

    @PUT("driver/modify-profile")
    suspend fun changeProfile(
        @Body body: ModifyProfileRequestBody
    ): Response<ResponseBody?>?

    @POST("handler/orders/items/list/pdf")
    suspend fun printPickList(
        @Body body: PrintPickListRequestBody,
        @Query("timezone") timezone: String? = TimeZone.getDefault().id.toString()
    ): Response<PrintAwbResponse?>?

    @GET("admin/fulfillment/totes/{toteBarcode}/order")
    suspend fun getOrderFromTote(
        @Path("toteBarcode") toteBarcode: String?,
    ): Response<FulfilmentOrder?>?

    @GET("handler/product/sub-bundle")
    suspend fun getSubBundlesProduct (
        @Query("productId") productId: Long,
        @Query("orderId") orderId: Long?
    ): Response<ArrayList<SubBundle?>>?

    @GET("handler/hub/reserved-bin")
    suspend fun scanBinBarcodeForChangeLocation (
        @Query("barcode") barcode: String
    ): Response<ResponseBody?>?

    @PUT("handler/hub/locations/{locationBarcode}/bin/sort")
    suspend fun scanNewLocationForChange (
        @Path("locationBarcode") locationBarcode: String,
        @Query("barcode") barcode: String
    ): Response<ResponseBody?>?
}
