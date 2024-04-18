package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.ReturnPackageRequestBody

interface CallDurationListener {
    fun saveCallDuration(callDuration: Double)
}