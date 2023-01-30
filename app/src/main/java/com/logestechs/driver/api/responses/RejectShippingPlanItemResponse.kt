package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.ShippingPlanDetails
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RejectShippingPlanItemResponse(
    var shippingPlanDetails: ShippingPlanDetails?
) : Parcelable