package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Vehicle(
    val originHubId: Int?,
    val volume: Double?,
    val agentName: String?,
    val plateNo: String?,
    val hubId: Int?,
    val workZone: String?,
    val driverId: Int?,
    val brand: String?,
    val barcode: String?,
    val destinationHubId: Long?,
    val id: Long?,
    val noOfPkgs: Int?,
    val name: String?,
    val companyId: Int?
) : Parcelable