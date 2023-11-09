package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.Customer

interface AcceptedPackagesCardListener {
    fun scanForPickup(customer: Customer?)
    fun getAcceptedPackages(customer: Customer?)

    fun printAwb()
}