package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.FulfilmentOrder

data class GetFulfilmentOrdersResponse(
    val data: List<FulfilmentOrder?>? = null,
    val total: Int? = null,
    val totalRecordsNo: Int? = null,
    val page: Int? = null
)
