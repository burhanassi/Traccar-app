package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.ServerLatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetDriverPackagesLocationsResponse(
    var geometry: String? = null,
    var items: List<List<ServerLatLng?>>? = null
) : Parcelable
