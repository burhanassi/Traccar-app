package com.logestechs.driver.api.requests

data class FailDeliveryRequestBody(
    val note: String?,
    val noteKey: String?,
    val deliveryProofUrlList: List<String?>?,
    @Transient
    val packageId: Long?
)