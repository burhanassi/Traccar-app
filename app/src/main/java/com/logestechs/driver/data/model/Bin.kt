package com.logestechs.driver.data.model

data class Bin(
    var barcode: String? = null,
    val barcodeImage: String? = null,
    val label: String? = null,
    val quantity: Int? = null,
    val isBarcodePrinted: Boolean? = null,
    val createdDate: String? = null,
    val id: Long? = null,
    val companyId: Int? = null
)