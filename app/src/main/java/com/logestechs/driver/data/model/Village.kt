package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Village(
    val name: String? = null,
    val id: Long? = null,

    val cityName: String? = null,
    @SerializedName("cityId")
    val cityID: Long? = null,

    val regionName: String? = null,
    @SerializedName("regionId")
    val regionID: Long? = null,

    ) : Parcelable