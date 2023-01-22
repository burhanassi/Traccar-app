package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShippingPlan(
    val createdDate: String? = null,
    val id: Long? = null,
    val companyId: Long? = null,
    val customerId: Long? = null,
    val shippingPlanStatus: String? = null,
    val driverId: Long? = null,
    val barcode: String? = null,
    val barcodeImage: String? = null,
    val notes: String? = null,
    val totalItemsQuantity: Int? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val customerAddress: String? = null,
    val customerBusinessName: String? = null,
    val driverName: String? = null
) : Parcelable
