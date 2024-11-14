package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Address(
    var addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,

    @SerializedName("cityId")
    val cityID: Long? = null,

    val country: String? = null,
    val village: String? = null,

    @SerializedName("villageId")
    val villageID: Long? = null,

    val region: String? = null,

    @SerializedName("regionId")
    val regionID: Long? = null,
    val id: Long? = null,
    var longitude: Double? = null,
    var latitude: Double? = null,
    var nationalAddress: String? = null,
    val locatedByReceiver: Boolean? = null
) : Parcelable {
    override fun toString(): String {
        return "$village - $city - $region"
    }

    fun toStringAddress(): String {
        return "$village - $city - $addressLine1"
    }

    companion object {
        fun getAddressFromVillage(village: Village, addressLine1: String): Address {
            return Address(
                addressLine1,
                null,
                village.cityName,
                village.cityID,
                "",
                village.name,
                village.id,
                village.regionName,
                village.regionID,
                null
            )
        }
    }
}