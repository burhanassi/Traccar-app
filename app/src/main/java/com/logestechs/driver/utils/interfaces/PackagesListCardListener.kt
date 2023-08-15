package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.Package

interface PackagesListCardListener {
    fun onShowPackageNoteDialog(pkg: Package?)
    fun onPickupPackage(barcode: String?)
}