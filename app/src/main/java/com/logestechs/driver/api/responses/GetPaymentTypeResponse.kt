package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.PaymentTypeModel

data class GetPaymentTypeResponse(
    val data: List<PaymentTypeModel>? = null,
    val total: Int? = null,
    val totalRecordsNo: Int? = null,
    val page: Int? = null
)
