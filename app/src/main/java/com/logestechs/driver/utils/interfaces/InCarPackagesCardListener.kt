package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.FailDeliveryRequestBody
import com.logestechs.driver.api.requests.PostponePackageRequestBody
import com.logestechs.driver.api.requests.ReturnPackageRequestBody

interface InCarPackagesCardListener {
    fun onPackageReturned(body: ReturnPackageRequestBody?)
    fun onFailDelivery(body: FailDeliveryRequestBody?)
    fun onPackagePostponed(body: PostponePackageRequestBody?)
}