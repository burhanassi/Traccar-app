package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.Village
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetVillagesResponse(
    val data: List<Village>,
    val totalRecordsNo: Long
) : Parcelable
