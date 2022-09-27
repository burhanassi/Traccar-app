package com.logestechs.driver.api.requests

data class UpdateLocationRequestBody(
    val latitude: Double?,
    val longitude: Double?,
    val companyId: Long?,
    val driverId: Long?,
    val vehicleId: Long?,
    val gpsReadingTime: String?
)
