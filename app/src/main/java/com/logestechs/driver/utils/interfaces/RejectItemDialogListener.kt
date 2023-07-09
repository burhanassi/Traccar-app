package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.PostponePackageRequestBody
import com.logestechs.driver.api.requests.RejectItemRequestBody
import com.logestechs.driver.api.responses.RejectItemResponse

interface RejectItemDialogListener {
    fun onItemRejected(rejectItemResponse: RejectItemRequestBody)
}