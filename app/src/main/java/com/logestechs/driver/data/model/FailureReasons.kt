package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FailureReasons(
    @SerializedName("POSTPONE")
    var postpone: LinkedHashMap<String, String>?,
    @SerializedName("RETURN")
    var returnShipment: LinkedHashMap<String, String>?,
    @SerializedName("FAIL")
    var fail: LinkedHashMap<String, String>?
) : Parcelable

