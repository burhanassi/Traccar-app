package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.utils.PackageType

interface PackageTypeFilterDialogListener {
    fun onPackageTypeSelected(selectedPackageType: PackageType)
}