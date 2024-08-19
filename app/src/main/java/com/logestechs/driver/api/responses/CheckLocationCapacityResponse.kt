package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.ItemDetails
import com.logestechs.driver.data.model.ShippingPlanDetails
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CheckLocationCapacityResponse(
    var isOverCapacity: Int?
) : Parcelable