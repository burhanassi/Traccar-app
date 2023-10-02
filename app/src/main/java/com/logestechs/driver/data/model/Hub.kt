package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Hub(
    var name: String? = null,
    var id: Long = 0,
    var addressId: Long = 0,
    var phone: String? = null,
    var contactPerson: String? = null
) : Parcelable