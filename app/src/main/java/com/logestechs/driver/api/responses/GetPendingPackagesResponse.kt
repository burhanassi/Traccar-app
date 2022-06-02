package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.Customer
import kotlinx.android.parcel.Parcelize


@Parcelize
data class GetPendingPackagesResponse(
    val customers: List<Customer?>? = null,
) : Parcelable