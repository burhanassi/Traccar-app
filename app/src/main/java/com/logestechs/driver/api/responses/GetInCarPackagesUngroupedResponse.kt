package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.Package

data class GetInCarPackagesUngroupedResponse(
    var pkgs: ArrayList<Package?>?,
    var codSum: Double?,
    var numberOfPackages: Int?
)
