package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShippingPlanDetails(
    var rejected: Int? = null,
    var sorted: Int? = null,
    var unsorted: Int? = null
) : Parcelable
