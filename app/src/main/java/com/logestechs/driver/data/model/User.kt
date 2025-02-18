package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    var phone: String? = null,
    val email: String? = null,

    @SerializedName("addressId")
    val addressID: Long? = null,

    val address: Address? = null,

    val city: String? = null,

    @SerializedName("hubId")
    val hubID: Long? = null,
    val hubName: String? = null,

    val role: String? = null,
    val country: String? = null,
    val barcode: String? = null,
    val barcodeImage: String? = null,

    @SerializedName("imageUrl")
    val imageURL: String? = null,
    var vehicleId: Long?,
    var vehicle: Vehicle?,
    val codSum: Double? = null,
    val codPackagesCount: Long? = null,
    val receivedMoney: Double? = null,
    val isAllowAddingAttachment: Boolean? = null,
    val isUseBankTransfer: Boolean? = null,
    val isExcludeSort: Boolean? = null,
    val isHubManagerAddPackage: Boolean? = null,
    val isNonAdminChangeCost: Boolean? = null,
    val isAllowOperationManagerAddCustomer: Boolean? = null,
    val companyLogo: String? = null,
    val isDeleted: Boolean? = null,
    val isAddCustomerFromQuickAddShipmentForm: Boolean? = null,
    val isDeliverPartialMassReport: Boolean? = null,
    val isAllowOperationManagerReceiveCod: Boolean? = null,
    val isPartialDeliveryEnabled: Boolean? = null,
    val isSupportReceiveWithoutReleasingCustody: Boolean? = null,
    val isSupportAddingDraftPackagesByCustomers: Boolean? = null,

    @SerializedName("isSendSmsToReceiversWhenDriverPickup")
    val isSendSMSToReceiversWhenDriverPickup: Boolean? = null,

    @SerializedName("isSendSmsToReceiversWhenDriverDeliver")
    val isSendSMSToReceiversWhenDriverDeliver: Boolean? = null,

    val isHideDeliveredMassCodReports: Boolean? = null,

    @SerializedName("isSendSmsToReceiverstoShareLocation")
    val isSendSMSToReceiverstoShareLocation: Boolean? = null,

    val followUpPackageCount: Long? = null,
    val isBundlePodEnabled: Boolean? = null,
    val isDriverCanRequestCodChange: Boolean? = null,
    val isDriverCanReturnPackage: Boolean? = null,
    val driverEarningSum: Double? = null,
    val packagesCount: Long? = null,
    val isFulfilmentEnabled: Boolean? = null,
    val isShowAddShipmentInSideMenu: Boolean? = null,
    val isDriverEarningEnabled: Boolean? = null,
    val isPickupSupported: Boolean? = null,
    val currency: String? = null,
    val createdDate: String? = null,
    val id: Long? = null,
    val password: String? = null,
    val isReturnPackages: Boolean? = null,
    val isPostponePackages: Boolean? = null,
    val isDeliverPackages: Boolean? = null,
    val isDistributor: Boolean? = null,
    val isTrucking: Boolean? = null,
    val isHideDeliveredToSenderInChangeStatus: Boolean? = null,
    val hasRouteOptimization: Boolean? = null,

    @SerializedName("companyId")
    val companyID: Long?,
    val andoidAdminAppMinVersion: String? = null,
    var isHideSenderInfo: Boolean? = null,
    var isShowSenderPhone: Boolean? = null,
    val driverCategoryName: String? = null,
    val optionalNumber: String? = null
) : Parcelable