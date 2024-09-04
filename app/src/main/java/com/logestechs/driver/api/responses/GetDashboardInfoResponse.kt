package com.logestechs.driver.api.responses


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetDashboardInfoResponse(
    @SerializedName("acceptedPackagesCount")
    var acceptedPackagesCount: Int? = null,
    @SerializedName("carriedCodSum")
    var carriedCodSum: Double? = null,
    @SerializedName("carriedMassReportsSum")
    var carriedMassReportsSum: Double? = null,
    @SerializedName("deliveredPackagesCount")
    var deliveredPackagesCount: Int? = null,
    @SerializedName("inCarPackagesCount")
    var inCarPackagesCount: Int? = null,
    @SerializedName("pendingPackagesCount")
    var pendingPackagesCount: Int? = null,
    @SerializedName("postponedPackagesCount")
    var postponedPackagesCount: Int? = null,
    @SerializedName("failedPackagesCount")
    var failedPackagesCount: Int? = null,
    var onlineStartTime: String? = null,
    var isDriverOnline: Boolean? = null,
    var driverEarningsSum: Double? = null
) : Parcelable