package com.logestechs.driver.api.requests


data class DeliverMassReturnedPackagesToSenderRequestBody(
    var barcode: String?,
    val signatureUrl: String?,
    val deliveryProofUrlList: List<String?>?,
)
