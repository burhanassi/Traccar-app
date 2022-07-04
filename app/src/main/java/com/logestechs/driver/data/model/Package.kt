package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.logestechs.driver.utils.BarcodeScanType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Package(
    //package info
    val id: Long? = null,
    val notes: String? = null,
    val status: String? = null,
    val customerStatusType: String? = null,
    val cod: Double? = null,
    val serviceType: String? = null,
    val shipmentType: String? = null,
    val isFragile: Boolean? = null,
    val isHazardousMaterial: Boolean? = null,
    val isNeedPackaging: Boolean? = null,
    val weight: Double? = null,
    val width: Double? = null,
    val length: Double? = null,
    val height: Double? = null,
    val cost: Double? = null,
    val invoiceNumber: String? = null,
    val createdDate: String? = null,
    val deliveryDate: String? = null,
    val barcode: String? = null,
    val barcodeImage: String? = null,
    val codBarcode: String? = null,
    val codBarcodeImage: String? = null,
    @SerializedName("parcelTypeId")
    val parcelTypeID: Long? = null,

    //products
    val quantity: Int? = null,

    //addresses
    val originAddress: Address? = null,
    val destinationAddress: Address? = null,

    @SerializedName("originAddressId")
    val originAddressID: Long? = null,

    @SerializedName("destinationAddressId")
    val destinationAddressID: Long? = null,

    //sender info
    val senderFirstName: String? = null,
    val senderMiddleName: String? = null,
    val senderLastName: String? = null,
    val senderName: String? = null,
    val senderEmail: String? = null,
    val senderPhone: String? = null,
    val businessSenderName: String? = null,
    val senderAuthorizedGovRegistrationNumber: String? = null,

    //receiver info
    val receiverFirstName: String? = null,
    val receiverMiddleName: String? = null,
    val receiverLastName: String? = null,
    val receiverName: String? = null,
    val receiverEmail: String? = null,
    val receiverPhone: String? = null,
    val receiverPhone2: String? = null,
    val businessReceiverName: String? = null,
    val receiverAuthorizedGovRegistrationNumber: String? = null,

    //customer
    val customerId: Long? = null,
    val customerName: String? = null,

    val isShowSenderAddressInReport: Boolean? = null,

    val isFailed: Boolean? = null,
    val failuresNumber: Long? = null,
    val isFailureResolved: Boolean? = null,
    val toCollectFromReceiver: Double? = null,
    val toPayToReceiver: Double? = null,
    val description: String? = null,
    val customerNotes: String? = null,
    val phoneType: String? = null,
    val isReceiverPayCost: Boolean? = null,

    @Transient
    var isSelected: Boolean = false,
    val expectedDeliveryDate: String? = null,
    val adminNotes: String? = null,
    var isDone: Boolean? = true,

    ) : Parcelable {
    fun getFullSenderName(): String {
        return if (senderMiddleName?.trim().isNullOrEmpty()) {
            "$senderFirstName $senderLastName"
        } else {
            "$senderFirstName $senderMiddleName $senderLastName"
        }
    }

    fun getFullReceiverName(): String {
        return if (receiverMiddleName?.trim().isNullOrEmpty()) {
            "$receiverFirstName $receiverLastName"
        } else {
            "$receiverFirstName $receiverMiddleName $receiverLastName"
        }
    }

    fun getPickupScannedItem(): ScannedItem {
        return ScannedItem(
            id = this.id,
            barcode = this.barcode,
            barcodeScanType = BarcodeScanType.PACKAGE_PICKUP,
            data = this
        )
    }
}