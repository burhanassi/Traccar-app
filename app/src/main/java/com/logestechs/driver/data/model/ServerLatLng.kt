package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ServerLatLng(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val label: String? = null
) : Parcelable