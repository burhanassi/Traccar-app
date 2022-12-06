package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.utils.ConfirmationDialogAction

interface ConfirmationDialogActionListener {
    fun confirmAction(data: Any?, action: ConfirmationDialogAction)
}