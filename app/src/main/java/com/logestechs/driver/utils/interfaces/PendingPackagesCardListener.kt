package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.RejectPackageRequestBody

interface PendingPackagesCardListener {
    fun acceptPackage(parentIndex: Int, childIndex: Int)
    fun acceptCustomerPackages(parentIndex: Int)
    fun rejectPackage(parentIndex: Int, childIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody)
    fun rejectCustomerPackages(parentIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody)
}