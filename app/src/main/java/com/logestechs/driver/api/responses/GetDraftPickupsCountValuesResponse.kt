package com.logestechs.driver.api.responses

data class GetDraftPickupsCountValuesResponse(
    var pendingCount: Int?,
    var acceptedCount: Int?,
    var inCarCount: Int?
)
