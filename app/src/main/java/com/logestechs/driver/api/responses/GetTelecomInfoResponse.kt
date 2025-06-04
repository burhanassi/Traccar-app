package com.logestechs.driver.api.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetTelecomInfoResponse(
    var supplierInvoice: String?,
    var thirdPartyTrackingNo: String?,
    var isFingerprintRequired: Boolean?,
    var thirdPartyBarcode: String?,
    var accountReferenceNumber: String?,
    var msisdn: String?,
    var simNumber: String?,
    var accountManagerName: String?,
    var accountManagerNumber: String?,
    var cr: String?
): Parcelable
