package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.DeficitBalanceHistory
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetDeficitBalanceHistoryResponse(
    val data: ArrayList<DeficitBalanceHistory>,
    val total: Double?,
    val totalRecordsNo: Int,
    val page: Int
) : Parcelable
