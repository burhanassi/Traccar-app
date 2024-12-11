package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.utils.DeliveredPackageStatus

interface DeliveredStatusFilterDialogListener {
    fun onStatusChanged(selectedStatus: DeliveredPackageStatus)
}