package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.ShippingPlan

interface DriverShippingPlanCardListener {
    fun onPickup(index: Int)
    fun onCancelPickup(index: Int, shippingPlan: ShippingPlan?)
}