package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PackageItemsToDeliver(
    var id: Long?,
    var name: String?,
    var cod: Double?,
    var status: Status?
) : Parcelable
enum class Status  {
    IN_PROGRESS,
    DELIVERED,
    RETURNED
}