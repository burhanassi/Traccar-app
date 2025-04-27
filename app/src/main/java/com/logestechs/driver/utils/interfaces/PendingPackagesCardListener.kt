package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.RejectPackageRequestBody
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Package

interface PendingPackagesCardListener {
    fun acceptPackage(parentIndex: Int, childIndex: Int)
    fun acceptCustomerPackages(parentIndex: Int)
    fun rejectPackage(parentIndex: Int, childIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody)
    fun rejectCustomerPackages(parentIndex: Int, rejectPackageRequestBody: RejectPackageRequestBody)
    fun onShowRejectPackageDialog(parentIndex: Int, childIndex: Int)
    fun onShowRejectCustomerPkgsDialog()
    fun onSendWhatsAppMessage(pkg: Package?, isSecondary: Boolean = false)
    fun onSendSmsMessage(pkg: Package?)
    fun onCallReceiver(pkg: Package?, receiverPhone: String?)
}