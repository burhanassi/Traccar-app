package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.AddNoteRequestBody

interface AddPackageNoteDialogListener {
    fun onPackageNoteAdded(addNoteRequestBody: AddNoteRequestBody?)
    fun onCaptureImage()
    fun onLoadImage()
    fun onDeleteImage(position: Int)
}