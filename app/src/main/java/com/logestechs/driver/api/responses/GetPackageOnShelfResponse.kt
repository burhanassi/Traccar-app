package com.logestechs.driver.api.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.logestechs.driver.data.model.Package
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetPackageOnShelfResponse(
    @SerializedName("dataObject")
    var packages: Package? = null,
    var responseCode: Int? = null,
    var data: String? = null
) : Parcelable