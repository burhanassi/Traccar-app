package com.logestechs.driver.api.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PayMultiWayResponse(
    val paymentStatus: String? = null,
    val toBePaid: Double? = null,
    val paidAmount: Double? = null
) : Parcelable
