package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.PostponePackageRequestBody

interface PostponePackageDialogListener {
    fun onPackagePostponed(body: PostponePackageRequestBody?)
    fun onCaptureImage()
    fun onLoadImage()
    fun onDeleteImage(position: Int)
}