package com.logestechs.driver.api.requests

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RejectItemRequestBody(
    val barcode: String?,
    val rejectReasonKey: String?,
    val rejectReason: String?,
    val urls: List<String?>?,
    var locationId: Long? = null
): Parcelable