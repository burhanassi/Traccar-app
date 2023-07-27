package com.logestechs.driver.api.requests

data class DeliverMassCodReportGroupRequestBody(
    val signatureUrl: String?,
    val deliveryProofUrlList: List<String?>?
)