package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MassCodReport(
    var createdDate: String? = null,
    var id: Long? = null,
    var companyID: Long? = null,
    var driverID: Long? = null,
    var accountantID: Long? = null,
    var barcode: String? = null,
    var barcodeImage: String? = null,
    var customerID: Long? = null,
    var status: String? = null,
    var customerName: String? = null,
    var customerCity: String? = null,
    var customerVillage: String? = null,
    var totalCod: Double? = null,
    var totalCost: Double? = null,
    var totalCodWithoutCost: Double? = null,
    var codPackagesNumber: Int? = null,
    var userName: String? = null,
    var paymentType: String? = null,
    var deliveryDate: String? = null,
    var exportDate: String? = null,
    var customerPhone: String? = null,
    var isPrintedByAccountant: Boolean? = null,
    var statusStringForExport: String? = null,
    var isCustomerBilled: Boolean? = null
) : Parcelable
