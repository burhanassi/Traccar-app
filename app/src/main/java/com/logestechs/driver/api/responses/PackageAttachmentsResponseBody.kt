package com.logestechs.driver.api.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PackageAttachmentsResponseBody(
    val imageUrls: List<String>?,
) : Parcelable