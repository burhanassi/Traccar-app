package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.data.model.FulfilmentOrder

interface FulfilmentOrderCardListener {
    fun onScanToteForOrder(fulfilmentOrder: FulfilmentOrder)
}