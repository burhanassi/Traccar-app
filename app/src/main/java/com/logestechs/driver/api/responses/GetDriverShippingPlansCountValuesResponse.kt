package com.logestechs.driver.api.responses

data class GetDriverShippingPlansCountValuesResponse(
    var pickedUpCount: Int? = null,
    var arrivedAtDestinationCount: Int? = null,
    var assignedToDriverCount: Int? = null,
    var rejectedCount: Int? = null
)
