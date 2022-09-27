package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeWorkLogStatusRequestBody(
    var isOnline: Boolean?,
    var id: Long?,
    var deviceId: Long?,
) : Parcelable