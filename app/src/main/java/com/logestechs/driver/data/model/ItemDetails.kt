package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemDetails(
    var productId: Long? = null,
    var name: String? = null,
    var sku: String? = null,
    var barcode: String? = null
) : Parcelable
