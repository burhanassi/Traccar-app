package com.logestechs.driver.utils.interfaces

import com.logestechs.driver.api.requests.*

interface InCarPackagesCardListener {
    fun onPackageReturned(body: ReturnPackageRequestBody?)
    fun onFailDelivery(body: FailDeliveryRequestBody?)
    fun onPackagePostponed(body: PostponePackageRequestBody?)
    fun onPackageTypeChanged(body: ChangePackageTypeRequestBody?)
    fun onPackageNoteAdded(body: AddNoteRequestBody?)
}