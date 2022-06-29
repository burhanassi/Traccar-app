package com.logestechs.driver.utils.interfaces

interface PendingPackagesCardListener {
    fun acceptPackage(parentIndex: Int, childIndex: Int)
    fun acceptCustomerPackages(parentIndex: Int)
    fun rejectPackage(parentIndex: Int, childIndex: Int)
    fun rejectCustomerPackages(parentIndex: Int)
}