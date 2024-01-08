package com.logestechs.driver.data.model

import android.os.Parcelable
import com.logestechs.driver.utils.FulfillmentOrderPackagingType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductItem(
    var createdDate: String? = null,
    var id: Long? = null,
    var companyId: Long? = null,
    var customerId: Long? = null,
    var productId: Long? = null,
    var fulfillmentOrderId: Long? = null,
    var barcode: String? = null,
    var barcodeImage: String? = null,
    var warehouseId: Long? = null,
    var name: String? = null,
    var expiryDate: String? = null,
    var sku: String? = null,
    var customerName: String? = null,
    var warehouseName: String? = null,
    var productName: String? = null,
    var quantity: Int? = null,
    var shippingPlanId: Long? = null,
    var binId: Long? = null,
    var isRejectedAfterSorting: Boolean? = null,
    var productImageUrl: String? = null,
    var price: Double? = null,
    var sum: Double? = null,
    var itemBinLocation: String? = null,
    var isCustomPackaging: Boolean? = null,
    var fulfillmentOrderPackagingType: FulfillmentOrderPackagingType? = null,
    var parcelTypeName: String? = null,
    var isBundle: Boolean? = null,
    var isExpanded: Boolean? = null
) : Parcelable
