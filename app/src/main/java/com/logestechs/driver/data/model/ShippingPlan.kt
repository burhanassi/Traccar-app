package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShippingPlan(
    var createdDate: String? = null,
    var id: Long? = null,
    var companyId: Long? = null,
    var customerId: Long? = null,
    var shippingPlanStatus: String? = null,
    var driverId: Long? = null,
    var barcode: String? = null,
    var barcodeImage: String? = null,
    var notes: String? = null,
    var totalItemsQuantity: Int? = null,
    var customerName: String? = null,
    var customerPhone: String? = null,
    var customerAddress: String? = null,
    var customerBusinessName: String? = null,
    var driverName: String? = null,
    var shippingPlanDetails: ShippingPlanDetails? = null
) : Parcelable
