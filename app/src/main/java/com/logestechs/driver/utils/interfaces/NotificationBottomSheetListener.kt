package com.logestechs.driver.utils.interfaces

interface NotificationBottomSheetListener {
    fun onRefresh()
    fun onSetNotificationAsRead(notificationId: Long)
}