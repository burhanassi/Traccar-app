package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.RejectItemRequestBody

interface ScannedShippingPlanItemCardListener {
    fun rejectItem(rejectItemRequestBody: RejectItemRequestBody)
}