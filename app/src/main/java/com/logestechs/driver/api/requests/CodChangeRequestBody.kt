package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class CodChangeRequestBody(
    val packageId: Long?,
    val newCodValue: Double?
) : Parcelable

