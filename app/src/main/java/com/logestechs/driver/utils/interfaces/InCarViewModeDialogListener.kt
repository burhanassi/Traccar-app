package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.utils.InCarPackagesViewMode

interface InCarViewModeDialogListener {
    fun onViewModeChanged(selectedViewMode: InCarPackagesViewMode)
}