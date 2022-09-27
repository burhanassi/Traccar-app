package com.logestechs.driver.api.responses

import com.logestechs.driver.data.model.DraftPickup

data class GetDraftPickupsResponse(
    var data: ArrayList<DraftPickup?>?,
    var totalRecordsNo: Int?,
    var page: Int?
)
