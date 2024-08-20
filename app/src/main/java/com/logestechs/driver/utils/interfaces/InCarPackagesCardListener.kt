package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.*
import com.logestechs.driver.data.model.Package

interface InCarPackagesCardListener {
    fun onPackageReturned(body: ReturnPackageRequestBody?)
    fun onShowReturnPackageDialog(pkg: Package?)
    fun onShowAttachmentsDialog(pkg: Package?)
    fun onFailDelivery(body: FailDeliveryRequestBody?)
    fun onPackagePostponed(body: PostponePackageRequestBody?)
    fun onPackageTypeChanged(body: ChangePackageTypeRequestBody?)
    fun onPackageWeightChanged(packageId: Long?, body: ChangePackageWeightRequestBody)
    fun onPackageNoteAdded(body: AddNoteRequestBody?)
    fun onShowFailDeliveryDialog(pkg: Package?)
    fun onShowPostponePackageDialog(pkg: Package?)
    fun onShowPackageNoteDialog(pkg: Package?)
    fun onCodChanged(body: CodChangeRequestBody?)
    fun onDeliverPackage(pkg: Package?, position: Int = 0)
    fun onSendWhatsAppMessage(pkg: Package?, isSecondary: Boolean = false)
    fun onSendSmsMessage(pkg: Package?)
    fun onCallReceiver(pkg: Package?, receiverPhone: String?)
    fun targetVerticalIndex(position: Int)
}