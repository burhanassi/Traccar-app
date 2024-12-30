package com.logestechs.driver.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeliverToWarehouseRequestBody(
    val orderIds: List<Long?>?,
    val notes: String? = null,
    val attachments: List<String?>? = null
) : Parcelable