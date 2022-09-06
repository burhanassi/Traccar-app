package com.logestechs.driver.api.responses

import com.google.gson.annotations.SerializedName
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.FailureReasons
import com.logestechs.driver.data.model.MessagingTemplates

data class GetDriverCompanySettingsResponse(
    @SerializedName("info")
    var driverCompanyConfigurations: DriverCompanyConfigurations? = null,
    var failureReasons: FailureReasons? = null,
    @SerializedName("templates")
    var messageDigest: MessagingTemplates? = null
)
