package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.ItemDetails
import com.logestechs.driver.data.model.ProductItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetPickedItemsResponse(
    val data: List<ProductItem>,
    val total: Double?,
    val totalRecordsNo: Int?,
    val page: Int?
): Parcelable
