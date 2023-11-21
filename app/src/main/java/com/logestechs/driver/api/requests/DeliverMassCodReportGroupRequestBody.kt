package com.logestechs.driver.api.requests

import com.google.gson.annotations.SerializedName

data class DeliverMassCodReportGroupRequestBody(
    val signatureUrl: String?,
    val deliveryProofUrlList: List<String?>?,
    @SerializedName("note")
    val notes: String? = null
)