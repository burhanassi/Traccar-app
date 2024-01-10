package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubBundle(
    val name: String?,
    val sku: String?,
    val subProductQuantity: Long?
) : Parcelable
