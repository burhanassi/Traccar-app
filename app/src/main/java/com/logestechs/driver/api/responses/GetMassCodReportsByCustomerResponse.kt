package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.GroupedMassCodReports

data class GetMassCodReportsByCustomerResponse(
    val massCodPackagesByCustomer: List<GroupedMassCodReports>? = null,
    val totalRecordsNo: Int? = null,
    val totalCod: Double? = null,
    val totalPackagesNumber: Int? = null,
)
