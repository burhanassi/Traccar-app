package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.data.model.WarehouseLocation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetReturnedItemsResponse(
    val data: List<ProductItem>,
    val total: Int? = null,
    val totalRecordsNo: Int? = null,
    val page: Int? = null
) : Parcelable
