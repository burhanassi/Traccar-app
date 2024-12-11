package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PayMultiWayRequestBody(
    val paymentType: String? = null,
    val paymentTypeId: Long? = null,
    val amount: Double,
) : Parcelable

