package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DriverRouteRequestBody(
    val ids: List<Long?>?
) : Parcelable