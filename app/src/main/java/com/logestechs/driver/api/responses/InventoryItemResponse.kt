package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.Device
import com.logestechs.driver.data.model.User
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InventoryItemResponse(
    val device: Device?,
    var user: User?,
    val businessName: String?,
    val email: String?,
    val authToken: String?,
) : Parcelable