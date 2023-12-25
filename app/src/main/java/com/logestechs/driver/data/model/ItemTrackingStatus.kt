package com.logestechs.driver.data.model

import android.os.Parcelable
import com.logestechs.driver.utils.AppLanguages
import com.logestechs.driver.utils.FulfillmentItemStatus
import kotlinx.android.parcel.Parcelize
@Parcelize
data class ItemTrackingStatus(
    var createdDate: String?,
    var status: FulfillmentItemStatus?,
    var note: String?,
    var arabicNote: String?
) : Parcelable {
    fun getStatusText(selectedLanguage: AppLanguages): String {
        return when (selectedLanguage) {
            AppLanguages.ENGLISH -> status?.english!!
            AppLanguages.ARABIC -> status?.arabic!!
        }
    }
}