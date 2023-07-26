package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.responses.PackageAttachmentsResponseBody

interface ShowAttachmentsDialogListener {
    fun showAttachments(packageId: Long?)
}