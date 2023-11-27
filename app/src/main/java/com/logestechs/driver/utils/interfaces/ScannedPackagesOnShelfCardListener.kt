package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.Package

interface ScannedPackagesOnShelfCardListener {
    fun onFlagPackage(packageId: Long)
    fun onUnFlagPackage(packageId: Long)
}