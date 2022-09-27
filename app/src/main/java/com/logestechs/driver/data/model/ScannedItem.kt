package com.logestechs.driver.data.model

import android.os.Parcelable
import com.logestechs.driver.utils.BarcodeScanType
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class ScannedItem(
    var id: Long? = null,
    var barcodeScanType: BarcodeScanType? = null,
    var barcode: String? = null,
    var data: @RawValue Any? = null
) : Parcelable