package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetFailureReasonsResponse(
    @SerializedName("POSTPONE")
    var postpone: LinkedHashMap<String, String>?,
    @SerializedName("RETURN")
    var returnShipment: LinkedHashMap<String, String>?,
    @SerializedName("FAIL")
    var fail: LinkedHashMap<String, String>?
) : Parcelable

