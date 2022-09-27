package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.Package

data class GetDeliveredPackagesResponse(
    var pkgs: ArrayList<Package?>?,
    var totalRecordsNo: Int?
)
