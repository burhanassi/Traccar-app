package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Customer(
    val id: Long? = null,
    val customerId: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val phone2: String? = null,
    val addressID: Long? = null,
    val hubID: Long? = null,
    val address: Address? = null,
    var packagesNo: Int? = null,
    val isDeleted: Boolean? = null,
    val isAutoApprovePackages: Boolean? = null,
    val isShowSenderAddressInPackageReport: Boolean? = null,
    var packages: ArrayList<Package?>? = null,
    val createdDate: String? = null,
    val companyID: Long? = null,
    val imageURL: String? = null,
    val verificationDate: String? = null,
    val businessName: String? = null,
    val city: String? = null,
    val customerName: String? = null,
    val packagesNumber: Int? = null,
    val isAlternateWhatsApp: Boolean? = null,
    val massReturnedPackagesReportBarcode: String? = null,
    @Transient
    var isExpanded: Boolean = false
) : Parcelable {
    fun getFullName(): String {
        return if (middleName?.trim().isNullOrEmpty()) {
            "$firstName $lastName"
        } else {
            "$firstName $middleName $lastName"
        }
    }
}
