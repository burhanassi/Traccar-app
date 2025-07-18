package com.logestechs.traccarApp.api.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.logestechs.traccarApp.data.model.Notification
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetNotificationsResponse(
    @SerializedName("data")
    val notificationsList: ArrayList<Notification>,
    val totalRecordsNo: Int,
    val unReadUserNotificationsNo: Int,
    val page: Int
) : Parcelable