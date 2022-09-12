package com.logestechs.driver.utils.interfaces

interface ReturnedPackagesCardListener {
    fun deliverPackage(parentIndex: Int, childIndex: Int)
    fun deliverCustomerPackages(parentIndex: Int)
    fun getCustomerPackages(parentIndex: Int)
}