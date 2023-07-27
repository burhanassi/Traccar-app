package com.logestechs.driver.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupedMassCodReports(
    var customerName: String?,
    var pkgs: ArrayList<MassCodReport?>,
    var address: String?,
    var codSum: Double?,
    var customerId: Long? ,
    @Transient
    var isExpanded: Boolean = false
) : Parcelable