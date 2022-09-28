package com.logestechs.driver.api.requests

import android.os.Parcelable
import com.logestechs.driver.data.model.Device
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LoginRequestBody(
    val email: String? = null,
    val password: String? = null,
    var businessName: String? = null,
    val device: Device? = null,
    var companyId: Long? = null

) : Parcelable

