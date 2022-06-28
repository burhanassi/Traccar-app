package com.logestechs.driver.utils.interfaces

interface PendingPackagesCardListener {
    fun acceptPackage(packageId: Long?)
    fun acceptCustomerPackages(customerId: Long?)
    fun rejectPackage(packageId: Long?)
    fun rejectCustomerPackages(customerId: Long?)
}