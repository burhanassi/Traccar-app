package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.Customer
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetCustomersWithReturnedPackagesResponse(
    var customers: ArrayList<Customer?>? = null,
) : Parcelable