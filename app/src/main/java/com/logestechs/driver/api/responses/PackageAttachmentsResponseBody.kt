package com.logestechs.driver.api.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PackageAttachmentsResponseBody(
    val imageUrls: List<String>?,
    @Transient
    val pkg: com.logestechs.driver.data.model.Package?
) : Parcelable