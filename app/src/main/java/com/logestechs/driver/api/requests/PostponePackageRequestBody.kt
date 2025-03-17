package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.TimeZone

@Parcelize
data class PostponePackageRequestBody(
    val driverId: Long?,
    val postponedDeliveryDate: String?,
    val note: String?,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val deliveryProofUrlList: List<String?>?,
    val timeZone: String? = TimeZone.getDefault().id.toString(),
    @Transient
    val packageId: Long?
) : Parcelable

