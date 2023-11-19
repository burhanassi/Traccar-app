package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.Package

interface ScannedPackagesOnShelfCardListener {
    fun onUnFlagPackage(packageId: Long)
    fun onShowPackageNoteDialog(pkg: Package?)

}