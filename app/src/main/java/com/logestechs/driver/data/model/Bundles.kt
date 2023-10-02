package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Bundles(
    var id: Long? = null,
    var barcode: String? = null,
    var packagesNumber: Int? = null,
    var cityName: String? = null,
    var customerName: String? = null,
    var packages: ArrayList<Package?>? = null,
    @Transient
    var isExpanded: Boolean = false
) : Parcelable