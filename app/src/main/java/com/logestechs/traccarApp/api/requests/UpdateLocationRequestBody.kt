package com.logestechs.traccarApp.api.requests

data class UpdateLocationRequestBody(
    val latitude: Double?,
    val longitude: Double?,
    val gpsReadingTime: String?
)
