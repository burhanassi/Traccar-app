package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.Package

interface ScannedBarcodeCardListener {
    fun onCancelPickup(
        position: Int,
        pkg: Package
    )

    fun onShowSubpackages(
        pkg: Package
    )
}