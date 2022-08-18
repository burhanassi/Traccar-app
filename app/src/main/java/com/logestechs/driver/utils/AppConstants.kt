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


        //permission codes
        const val PERMISSIONS_REQUEST_PHONE_CALL = 1
        const val PERMISSIONS_REQUEST_PHONE_CAMERA = 2
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3
        const val PERMISSIONS_REQUEST_SEND_SMS = 4
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

enum class DateFormats(val value: String) {
    DEFAULT_FORMAT("yyyy-MM-dd"),
    SERVER_FORMAT("yyyy-MM-dd'T'HH:mm:ss"),
    TRACK_NODE_DATE_FORMAT("yy-MM-dd"),
    TRACK_NODE_TIME_FORMAT("hh:mm aa"),
    NOTIFICATION_LIST_ITEM_FORMAT("dd-MM-yyyy, hh:mm aa"),
    NOTIFICATION_DAY_FORMAT("EEEE"),
    DATE_FILTER_FORMAT("yyyy/MM/dd")

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

enum class IntentExtrasKeys() {
    SELECTED_PACKAGES_TAB,
    CUSTOMER_WITH_PACKAGES_FOR_PICKUP,
    SINGLE_SCAN_BARCODE_SCANNER_LISTENER,
    PACKAGE_TO_DELIVER
}

enum class BarcodeScanType {
    PACKAGE_PICKUP
}

enum class PackageType {
    COD,
    REGULAR,
    SWAP,
    BRING
}

enum class InCarPackagesViewMode(val value: String) {
    PACKAGES("PACKAGES"),
    BY_VILLAGE("BY_VILLAGE"),
    BY_CUSTOMER("BY_CUSTOMER"),
    BY_RECEIVER("BY_RECEIVER")
}

enum class InCarPackageStatus(val value: String) {
    ALL("ALL"),
    TO_DELIVER("TO_DELIVER"),
    RETURNED("RETURNED"),
    POSTPONED("POSTPONED"),
    COD("COD"),
    FAILED("FAILED")
}