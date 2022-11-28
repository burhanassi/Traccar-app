package com.logestechs.driver.api.requests

data class AddNoteRequestBody(
    val note: String?,
    val deliveryProofUrlList: List<String?>?,
    @Transient
    val packageId: Long?
)