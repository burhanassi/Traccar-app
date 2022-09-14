package com.logestechs.driver.data.model

data class DraftPickup(
    var barcode: String?,
    var barcodeImage: String?,
    var customerId: Int?,
    var status: String?,
    var notes: String?,
    var driverId: Long?,
    var hubId: Long?,
    var authorId: Long?,
    var customerName: String?,
    var pickupsNumber: Int?,
    var address: Address?,
    var customerPhone: String?,
    var createdDate: String?,
    var id: Long?,
    var companyId: Long?
)
