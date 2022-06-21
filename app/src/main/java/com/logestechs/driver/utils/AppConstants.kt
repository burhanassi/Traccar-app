package com.logestechs.driver.utils

import com.logestechs.driver.BuildConfig

class AppConstants {
    companion object {
        const val SPLASH_TIME_OUT: Long = 2000
        const val DATE_PICKER_OPEN_DELAY: Long = 1000
        const val FAILURE_REASONS_UPDATE_INTERVAL: Long = 1

        const val PACKAGE_NAME = BuildConfig.APPLICATION_ID

        const val BASE_URL = BuildConfig.base_url
        const val PATH = BuildConfig.path


        const val ERROR_KEY = "error"

        //Default Api Params
        const val DEFAULT_PAGE_SIZE = 50
        const val DEFAULT_PAGE = 1
    }
}

enum class AppLanguages(val value: String) {
    ARABIC("ar"),
    ENGLISH("en")
}

enum class IntentAnimation(val value: String) {
    RTL("right-to-left"),
    LTR("left-to-right")
}

enum class AppCurrency(val value: String) {
    NIS("₪"),
    JOD("JOD"),
    BHD("BHD"),
    KWD("KWD"),
    IQD("IQD"),
    SAR("SAR"),
    LYD("LYD"),
    OMR("OMR")
}