package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.ReturnPackageRequestBody

interface ReturnPackageDialogListener {
    fun onPackageReturned(returnPackageRequestBody: ReturnPackageRequestBody?)
    fun onCaptureImage()
    fun onLoadImage()
    fun onDeleteImage(position: Int)
}