package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostponePackageRequestBody(
    val note: String?,
    val noteKey: String?,
    val postponedDeliveryDate: String?,
    @Transient
    val packageId: Long?
) : Parcelable

