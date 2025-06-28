package com.logestechs.traccarApp.api.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class LoginResponse(
    val id: Long?,
    val name: String?,
    val email: String?

    ) : Parcelable