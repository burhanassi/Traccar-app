package com.logestechs.traccarApp.api.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LoginRequestBody(
    val email: String? = null,
    val password: String? = null,

) : Parcelable

