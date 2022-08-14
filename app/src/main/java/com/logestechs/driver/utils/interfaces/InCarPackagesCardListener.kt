package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.ReturnPackageRequestBody

interface InCarPackagesCardListener {
    fun onPackageReturned(body: ReturnPackageRequestBody?)
}