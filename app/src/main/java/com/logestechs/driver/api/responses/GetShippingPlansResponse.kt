package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.ShippingPlan

data class GetShippingPlansResponse(
    val data: List<ShippingPlan?>? = null,
    val total: Int? = null,
    val totalRecordsNo: Int? = null,
    val page: Int? = null
)