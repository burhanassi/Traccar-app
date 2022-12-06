package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReturnPackageRequestBody(
    val note: String?,
    val noteKey: String?,
    val isCostPaid: Boolean?,
    @Transient
    val pkg: com.logestechs.driver.data.model.Package?
) : Parcelable

