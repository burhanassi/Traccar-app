package com.logestechs.driver.utils.interfaces

interface PendingDraftPickupCardListener {
    fun onAcceptDraftPickup(index: Int)
    fun onRejectDraftPickup(index: Int)
}