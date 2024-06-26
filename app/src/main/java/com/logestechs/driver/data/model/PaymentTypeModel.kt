package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaymentTypeModel (
    var id: Long? = null,
    var name: String? = null,
    var arabicName: String? = null
):Parcelable