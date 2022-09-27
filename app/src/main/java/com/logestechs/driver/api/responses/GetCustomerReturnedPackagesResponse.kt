package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.Package

data class GetCustomerReturnedPackagesResponse(
    var pkgs: ArrayList<Package?>?
)
