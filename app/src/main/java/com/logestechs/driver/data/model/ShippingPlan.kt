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
    var shippingPlanDetails: ShippingPlanDetails? = null,
    var rejected: Int? = null,
    var sorted: Int? = null,
    var unsorted: Int? = null,
    var numberOfSkus: Int? = null,
    var totalQuantity: Int? = null,
    var numberOfBoxes: Int? = null,
    var warehouseName: String? = null

) : Parcelable {
    fun groupShippingPlanDetails() {
        this.shippingPlanDetails = ShippingPlanDetails(this.rejected, this.sorted, this.unsorted)
    }
}
