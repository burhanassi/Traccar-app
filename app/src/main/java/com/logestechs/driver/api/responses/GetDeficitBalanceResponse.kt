package com.logestechs.driver.api.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GetDeficitBalanceResponse(
    var id: Long,
    var amount: Double? = null,
) : Parcelable
