package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WarehouseLocation(
    val barcode: String?,
    val barcodeImage: String?,
    val label: String?,
    val shelfId: Int?,
    val rowId: Int?,
    val hubId: Int?,
    val createdDate: String?,
    val id: Long?,
    val companyId: Int?
) : Parcelable, DropdownItem() {
    override fun toString(): String {
        return "${barcode}"
    }
}