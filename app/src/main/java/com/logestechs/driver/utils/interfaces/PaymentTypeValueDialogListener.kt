package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.utils.customViews.StatusSelector

interface PaymentTypeValueDialogListener {
    fun onValueInserted(value: Double, selectedPaymentType: StatusSelector?, paymentTypeId: Long?)
}