package com.logestechs.driver.api.requests

data class ChangePackageTypeRequestBody(
    val shipmentType: String?,
    @Transient
    val packageId: Long?
)
