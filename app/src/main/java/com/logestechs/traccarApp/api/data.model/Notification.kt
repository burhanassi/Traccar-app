package com.logestechs.traccarApp.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Notification(
    val userId: Long,

    @SerializedName("packageId")
    val packageID: Long,

    @SerializedName("messageId")
    val messageID: Long,

    val title: String,
    val senderName: String,
    val body: String,
    val titleArabic: String,
    val bodyArabic: String,
    var isRead: Boolean,
    val type: String,
    val isDeleted: Boolean,
    val createdDate: String,
    val id: Long,
    var isExpanded: Boolean = false

) : Parcelable
