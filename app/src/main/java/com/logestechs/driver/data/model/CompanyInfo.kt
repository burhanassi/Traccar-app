package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CompanyInfo(
    val name: String? = null,

    @SerializedName("addressId")
    val addressID: Long? = null,
    val address: Address? = null,
    val phone: String? = null,
    val placedPackagesNumber: Long? = null,
    val pickedPackagesNumber: Long? = null,
    val deliveredPackagesNumber: Long? = null,
    val isGuestAddPackage: Boolean? = null,
    val domain: String? = null,
    val isUseBankTransfer: Boolean? = null,
    val isShowPhoneInPackageReport: Boolean? = null,
    val isDriverPickupUnassignedPackage: Boolean? = null,
    val isExcludeSort: Boolean? = null,
    val isAdminOnlyChangeFromDelivered: Boolean? = null,
    val isShowDriverNameInPackageTracking: Boolean? = null,
    val isChangeReceivedOrSortedPackageStatus: Boolean? = null,
    val isHubManagerAddPackage: Boolean? = null,
    val isNonAdminChangeCost: Boolean? = null,
    val id: Long? = null,
    val customerAndoidUrl: String? = null,
    val isTrucking: Boolean? = null,
    var isDistributor: Boolean? = null,
    var hasRouteOptimization: Boolean? = null,
    var currency: String? = null,
    var logo: String? = null,
    var isDriverSignupEnabled: Boolean? = null
) : Parcelable
