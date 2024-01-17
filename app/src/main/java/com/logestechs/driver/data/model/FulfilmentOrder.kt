package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FulfilmentOrder(
    val createdDate: String? = null,
    val id: Long? = null,
    val companyId: Long? = null,
    val customerId: Long? = null,
    val driverId: Long? = null,
    val warehouseId: Long? = null,
    val receiverType: String? = null,
    val receiverName: String? = null,
    val barcode: String? = null,
    val barcodeImage: String? = null,
    val receiverAddress: Address? = null,
    val receiverPhone: String? = null,
    val receiverPhone2: String? = null,
    val companyName: String? = null,
    val registrationNumber: String? = null,
    val isDeliverToReceivingPoint: Boolean? = null,
    val notes: String? = null,
    val packagingType: String? = null,
    val shipmentType: String? = null,
    val paymentType: String? = null,
    val cod: Double? = null,
    val cost: Double? = null,
    val customerShouldPayDeliveryCost: Boolean? = null,
    val receiverPayTypeValue: String? = null,
    val status: String? = null,
    val items: List<ProductItem?>? = null,
    val warehouseName: String? = null,
    val customerName: String? = null,
    val numberOfItems: Int? = null,
    val totalQuantity: Int? = null,
    val totBarcode: String? = null,
    val numberOfSkus: Int? = null
) : Parcelable
