package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.FulfilmentOrder

interface PickedFulfilmentOrderCardListener {
    fun onPackFulfilmentOrder(index: Int)
    fun onContinuePickingClicked(fulfilmentOrder: FulfilmentOrder?)
}