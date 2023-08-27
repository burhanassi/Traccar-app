package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.utils.ReturnedPackageStatus

interface ReturnedStatusFilterDialogListener {
    fun onStatusChanged(selectedStatus: ReturnedPackageStatus)
}