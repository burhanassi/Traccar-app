package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RejectPackageRequestBody(
    val note: String?,
    val deliveryProofUrlList: List<String?>?
): Parcelable

