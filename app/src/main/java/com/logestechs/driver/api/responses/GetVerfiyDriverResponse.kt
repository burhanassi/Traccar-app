package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.Vehicle
import com.logestechs.driver.data.model.Village
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetVerfiyDriverResponse(
    val id: Long? = null,
    val city: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val vehicle: Vehicle? = null,
    val driverPkgs: ArrayList<Package?>
) : Parcelable