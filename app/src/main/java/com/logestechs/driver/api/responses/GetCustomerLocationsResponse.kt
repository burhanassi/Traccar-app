package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.WarehouseLocation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetCustomerLocationsResponse(
    val data: List<WarehouseLocation>,
    val totalRecordsNo: Long
) : Parcelable
