package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PayMultiWayRequestBody(
    val paymentType: String?,
    val paymentTypeId: Long?,
    val amount: Double,
) : Parcelable

