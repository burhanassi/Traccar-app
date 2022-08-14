package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.FailDeliveryRequestBody

interface FailDeliveryDialogListener {
    fun onFailDelivery(body: FailDeliveryRequestBody?)
}