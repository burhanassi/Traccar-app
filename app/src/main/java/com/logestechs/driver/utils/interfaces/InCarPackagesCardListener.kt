package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.Package

interface InCarPackagesCardListener {
    fun onPackageReturned(pkg: Package?)
}