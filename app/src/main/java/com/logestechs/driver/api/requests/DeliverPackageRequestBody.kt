package com.logestechs.driver.api.requests

import com.logestechs.driver.data.model.PackageItemsToDeliver

data class DeliverPackageRequestBody(
    val packageId: Long?,
    val longitude: Double?,
    val latitude: Double?,
    val altitude: Double?,
    val precision: Double?,
    val signatureUrl: String?,
    val deliveryProofUrlList: List<String?>?,
    val subBundlesIds: List<Long?>?,
    val cod: Double?,
    val paymentType: String?,
    val paymentTypeId: Long?,
    val packageItemsToDeliverList: List<PackageItemsToDeliver?>?
)