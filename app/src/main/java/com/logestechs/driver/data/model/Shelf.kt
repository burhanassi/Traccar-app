package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Shelf(
    var createdDate: String? = null,
    var id: Long? = null,
    var companyId: Long? = null,
    var hubId: Long? = null,
    var barcode: String? = null,
    var barcodeImage: String? = null,
) : Parcelable