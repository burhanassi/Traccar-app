package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.utils.FulfillmentOrderPackagingType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PackFulfilmentOrderByItemResponse(
    var barcode: String?,
    var expiryDate: String?,
    var sku: String?,
    var productName: String?,
    var isCustomPackaging: Boolean?,
    var fulfillmentOrderPackagingType: FulfillmentOrderPackagingType,
    var parcelTypeNameval: String?,
    var barcodeImage: String?
) : Parcelable