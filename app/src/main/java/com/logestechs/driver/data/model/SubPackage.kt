package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubPackage(
    var invoiceNumber: String? = null,
    var weight: Double? = null,
    val id: Long? = null,
    val barcode: String? = null,
    val packageId: Long? = null,
    val companyId: Long? = null,
    var width: Int? = null,
    var length: Int? = null,
    var height: Int? = null,
    var viewId: Int? = null,
) : Parcelable
