package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.GroupedPackages

data class GetInCarPackagesGroupedResponse(
    var inCarPackages: ArrayList<GroupedPackages?>?,
    var codSum: Double?,
    var numberOfPackages: Int?
)