package com.logestechs.driver.api.requests

data class DeliverReturnedPackageToSenderRequestBody(
    var ids: ArrayList<Long?>? = null,
    val signatureUrl: String?,
    val deliveryProofUrlList: List<String?>?,
)
