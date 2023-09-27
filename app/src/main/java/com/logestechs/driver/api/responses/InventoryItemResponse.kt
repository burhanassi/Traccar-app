package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.Device
import com.logestechs.driver.data.model.User
import com.logestechs.driver.utils.FulfillmentItemStatus
import com.logestechs.driver.utils.ProductItemRejectReasonKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InventoryItemResponse(
    var barcode: String?,
    var expiryDate: String?,
    var SKU: String?,
    var customerName: String?,
    var warehouseName: String?,
    var productName: String?,
    var binBarcode: String?,
    var locationBarcode: String?,
    var shippingPlanBarcode: String?,
    var previousLocationBarcode: String?,
    var previousBinBarcode: String?,
    var rejectReason: String?,
    var rejectReasonKey: ProductItemRejectReasonKey?,
    var sortingDate: String?,
    var createdDate: String?,
    var pickedUser: String?,
    var packedUser: String?,
    var orderBarcode: String?,
    var status: FulfillmentItemStatus,
    var packageBarcode: String?,
    var toteBarcode: String?
) : Parcelable