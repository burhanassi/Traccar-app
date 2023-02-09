package com.logestechs.driver.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.logestechs.driver.utils.BarcodeScanType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Package(
    //package info
    var id: Long? = null,
    var notes: String? = null,
    var status: String? = null,
    var customerStatusType: String? = null,
    var cod: Double? = null,
    var serviceType: String? = null,
    var shipmentType: String? = null,
    var isFragile: Boolean? = null,
    var isHazardousMaterial: Boolean? = null,
    var postponedDeliveryDate: String? = null,
    var isNeedPackaging: Boolean? = null,
    var weight: Double? = null,
    var width: Double? = null,
    var length: Double? = null,
    var height: Double? = null,
    var cost: Double? = null,
    var invoiceNumber: String? = null,
    var createdDate: String? = null,
    var deliveryDate: String? = null,
    var barcode: String? = null,
    var barcodeImage: String? = null,
    var codBarcode: String? = null,
    var codBarcodeImage: String? = null,
    @SerializedName("parcelTypeId")
    var parcelTypeID: Long? = null,
    var destinationCity: String? = null,
    var partnerId: Long? = null,

    //products
    var quantity: Int? = null,

    //addresses
    var originAddress: Address? = null,
    var destinationAddress: Address? = null,

    @SerializedName("originAddressId")
    var originAddressID: Long? = null,

    @SerializedName("destinationAddressId")
    var destinationAddressID: Long? = null,

    //sender info
    var senderFirstName: String? = null,
    var senderMiddleName: String? = null,
    var senderLastName: String? = null,
    var senderName: String? = null,
    var senderEmail: String? = null,
    var senderPhone: String? = null,
    var senderPhone2: String? = null,
    var businessSenderName: String? = null,
    var senderAuthorizedGovRegistrationNumber: String? = null,

    //receiver info
    var receiverFirstName: String? = null,
    var receiverMiddleName: String? = null,
    var receiverLastName: String? = null,
    var receiverName: String? = null,
    var receiverEmail: String? = null,
    var receiverPhone: String? = null,
    var receiverPhone2: String? = null,
    var businessReceiverName: String? = null,
    var receiverAuthorizedGovRegistrationNumber: String? = null,

    //customer
    var customerId: Long? = null,
    var customerName: String? = null,

    var isShowSenderAddressInReport: Boolean? = null,

    var isFailed: Boolean? = null,
    var failuresNumber: Long? = null,
    var isFailureResolved: Boolean? = null,
    var toCollectFromReceiver: Double? = null,
    var toPayToReceiver: Double? = null,
    var description: String? = null,
    var customerNotes: String? = null,
    var phoneType: String? = null,
    var isReceiverPayCost: Boolean? = null,

    @Transient
    var isSelected: Boolean = false,
    var expectedDeliveryDate: String? = null,
    var adminNotes: String? = null,
    var isDone: Boolean? = true,
    @Transient
    var scannedSubPackagesCount: Int = 0

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