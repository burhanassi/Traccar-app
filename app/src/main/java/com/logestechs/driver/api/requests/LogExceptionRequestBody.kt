package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LogExceptionRequestBody(
    val url: String,
    val requestBody: String,
    val responseBody: String,
    val operatingSystem: String,
    val deviceInfo: String,
) : Parcelable

