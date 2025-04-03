package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeficitBalanceHistory(
    val createdDate: String,
    val id: Long,
    val companyId: Long,
    val deficitBalanceId: Long,
    val type: String,
    val source: String,
    val sourceId: Long,
    val amount: Double,
    val createdById: Long,
    val createdBy: String,
    val updatedAt: String,
    val updatedById: Long,
    val updatedBy: String,
    val msg: String,
    val barcode: String
) : Parcelable
