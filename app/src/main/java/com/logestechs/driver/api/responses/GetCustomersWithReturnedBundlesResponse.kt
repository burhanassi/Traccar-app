package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.logestechs.driver.data.model.Bundles
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetCustomersWithReturnedBundlesResponse(
    @SerializedName("data")
    var bundles: ArrayList<Bundles?>? = null,
) : Parcelable
