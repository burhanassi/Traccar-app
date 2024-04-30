package com.logestechs.driver.data.model

data class Bin(
    var barcode: String? = null,
    var barcodeImage: String? = null,
    var label: String? = null,
    var quantity: Int? = null,
    var isBarcodePrinted: Boolean? = null,
    var createdDate: String? = null,
    var id: Long? = null,
    var companyId: Int? = null,
    var isReserved: Boolean? = null,
    var isTote: Boolean? = null,
    var locationId: Long? = null
)