package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.Package

interface ReturnPackageDialogListener {
    fun onPackageReturned(pkg: Package?)
}