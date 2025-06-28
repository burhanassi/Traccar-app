package com.logestechs.traccarApp.api.requests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceRequest(
val name: String,
val uniqueId: String,
val status: String = "unknown"
) : Parcelable
