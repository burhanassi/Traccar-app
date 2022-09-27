package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.Village

data class GetAcceptedPackagesResponse(
    var villages: ArrayList<Village?>? = null
)