package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.utils.InCarPackageStatus

interface InCarStatusFilterDialogListener {
    fun onStatusChanged(selectedStatus: InCarPackageStatus)
}