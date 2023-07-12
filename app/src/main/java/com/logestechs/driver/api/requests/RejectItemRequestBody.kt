package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RejectItemRequestBody(
    val barcode: String?,
    val rejectReasonKey: String?,
    val rejectReason: String?
): Parcelable