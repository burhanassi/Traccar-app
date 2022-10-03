package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Village(
    val name: String,
    val id: Long,
    val cityName: String,
    @SerializedName("cityId")
    var cityID: Long? = null,

    var regionName: String? = null,
    @SerializedName("regionId")
    var regionID: Long? = null,
    var packages: List<Package?>? = null,
    var customers: List<Customer?>? = null,
    var numberOfPackages: Int? = null,
    var isExpanded: Boolean = false

) : Parcelable, DropdownItem() {
    override fun toString(): String {
        return "${name} - ${cityName} - ${regionName}"
    }
}
