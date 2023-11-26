package com.logestechs.driver.api.requests

data class ModifyProfileRequestBody(
    val phone: String?,
    val password: String?,
    val oldPassword: String?
)