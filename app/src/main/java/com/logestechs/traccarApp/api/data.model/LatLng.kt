package com.logestechs.traccarApp.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LatLng(
    var lat: Double?,
    var lng: Double?
) : Parcelable {
    fun toGoogleLatLng(): LatLng {
        return LatLng(this.lat ?: 0.0, this.lng ?: 0.0)
    }
}