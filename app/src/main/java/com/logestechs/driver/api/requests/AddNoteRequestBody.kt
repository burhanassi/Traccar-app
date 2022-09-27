package com.logestechs.driver.api.requests

data class AddNoteRequestBody(
    val note: String?,
    @Transient
    val packageId: Long?
)