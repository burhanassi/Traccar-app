package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Timestamp

@Parcelize
data class CheckIns(
    var hubId: Long = 0,
    var notes: String? = null,
    var type: Type = Type.DELIVERY,
    var isAutomatic: Boolean = false,
    var timestamp: String? = null,
    var vehicleName: String? = null,
    var hub: Hub? = null
) : Parcelable

enum class Type {
    PICKUP, DELIVERY
}