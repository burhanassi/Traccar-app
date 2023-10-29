package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(
    var id: Long? = null,
    var name: String? = null
) : Parcelable