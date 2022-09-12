package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.MassCodReport

data class GetMassCodReportsResponse(
    val massCodPackages: List<MassCodReport>? = null,
    val totalRecordsNo: Int? = null,
    val totalCod: Double? = null,
    val totalPackagesNumber: Int? = null,
    val costSum: Double? = null
)
