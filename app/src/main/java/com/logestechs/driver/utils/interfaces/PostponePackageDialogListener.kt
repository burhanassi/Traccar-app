package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.PostponePackageRequestBody

interface PostponePackageDialogListener {
    fun onPackagePostponed(postponePackageRequestBody: PostponePackageRequestBody)
}