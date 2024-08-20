package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.ChangePackageWeightRequestBody

interface ChangePackageWeightDialogListener {
    fun onPackageWeightChanged(packageId: Long?, body: ChangePackageWeightRequestBody)
}