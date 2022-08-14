package com.logestechs.driver.api.requests

data class FailDeliveryRequestBody(
    val note: String?,
    val noteKey: String?,
    @Transient
    val packageId: Long?
)