package com.logestechs.driver.api.requests

import android.os.Parcelable
import com.logestechs.driver.data.model.Address
import com.logestechs.driver.data.model.Device
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SignUpRequestBody(
    val firstName: String?,
    val companyName: String?,
    val email: String?,
    val phone: String?,
    val password: String?,
    val address: Address?,
    val device: Device? = null
) : Parcelable
