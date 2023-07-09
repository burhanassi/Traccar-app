package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.RejectPackageRequestBody

interface RejectPackageDialogListener {
    fun onPackageRejected(rejectPackageRequestBody: RejectPackageRequestBody)
}