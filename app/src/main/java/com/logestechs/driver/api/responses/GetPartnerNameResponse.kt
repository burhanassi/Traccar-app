package com.logestechs.driver.api.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetPartnerNameResponse(
    var name: String?
) : Parcelable