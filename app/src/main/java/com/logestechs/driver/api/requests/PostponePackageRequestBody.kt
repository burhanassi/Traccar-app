package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostponePackageRequestBody(
    val note: String?,
    val noteKey: String?,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val postponedDeliveryDate: String?,
    val deliveryProofUrlList: List<String?>?,
    @Transient
    val packageId: Long?
) : Parcelable

