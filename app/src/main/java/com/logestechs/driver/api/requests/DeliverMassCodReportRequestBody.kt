package com.logestechs.driver.api.requests

data class DeliverMassCodReportRequestBody(
    val signatureUrl: String?,
    val deliveryProofUrlList: List<String?>?
)