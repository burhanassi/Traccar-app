package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScanDraftPickupBarcodesRequestBody(
    var barcodes: ArrayList<String?>
) : Parcelable
