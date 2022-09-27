package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Device(
    @SerializedName("UUID")
    val uuid: String? = null,
    val operatingSystem: String? = null,
    var notificationToken: String? = null,
    var id: Long? = null
) : Parcelable {
    constructor(uuid: String, operatingSystem: String) : this(uuid, operatingSystem, null)
}
