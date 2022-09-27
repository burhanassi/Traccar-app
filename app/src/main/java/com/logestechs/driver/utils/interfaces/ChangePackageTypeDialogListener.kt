package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.ChangePackageTypeRequestBody

interface ChangePackageTypeDialogListener {
    fun onPackageTypeChanged(changePackageTypeRequestBody: ChangePackageTypeRequestBody)
}