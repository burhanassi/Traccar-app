package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Village

interface AcceptedPackagesCardListener {
    fun scanForPickup(customer: Customer?)
    fun getAcceptedPackages(customer: Customer?, village: Village?)
    fun printAwb(customer: Customer?)
}