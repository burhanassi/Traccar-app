package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetFailureReasonsResponse(
    @SerializedName("POSTPONE")
    var postpone: HashMap<String, String>?,
    @SerializedName("RETURN")
    var returnShipment: HashMap<String, String>?,
    @SerializedName("FAIL")
    var fail: HashMap<String, String>?
) : Parcelable

