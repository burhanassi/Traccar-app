package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostponePackageRequestBody(
    val driverId: Long?,
    val postponedDeliveryDate: String?,
    val note: String?,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val deliveryProofUrlList: List<String?>?,
    @Transient
    val packageId: Long?
) : Parcelable

