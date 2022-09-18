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
        const val DEFAULT_PAGE_SIZE = 20
        const val DEFAULT_PAGE = 1

        //request codes
        const val REQUEST_TAKE_PHOTO = 4001
        const val REQUEST_LOAD_PHOTO = 4002
        const val REQUEST_CAMERA_AND_STORAGE_PERMISSION = 5001
        const val REQUEST_STORAGE_PERMISSION = 5002
        const val REQUEST_LOCATION_PERMISSION = 5003

        //permission codes
        const val PERMISSIONS_REQUEST_PHONE_CALL = 1
        const val PERMISSIONS_REQUEST_PHONE_CAMERA = 2
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3
        const val PERMISSIONS_REQUEST_SEND_SMS = 4

        const val IMAGE_FULL_QUALITY = 50
    }
}

enum class BundleKeys {
    NOTIFICATIONS_KEY, UNREAD_NOTIFICATIONS_COUNT
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

enum class DraftPickupStatus {
    ASSIGNED_TO_DRIVER,
    ACCEPTED_BY_DRIVER,
    IN_CAR
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

enum class PaymentType(val englishLabel: String, val arabicLabel: String) {
    CASH("Cash", "نقداً"),
    CHEQUE("Cheque", "شيك"),
    BANK_TRANSFER("Bank Transfer", "حوالة بنكية"),
    PREPAID("Prepaid", "دفع مسبق"),
    DIGITAL_WALLET("Digital Wallet", "محفظة الكترونية"),
    CARD_PAYMENT("Card Payment", "بطاقة ائتمان")
}

enum class DeliveryType {
    FULL,
    PARTIAL
}

enum class SmsTemplateTag(val tag: String) {
    NAME("<الاسم>"),
    barcode("<باركود>"),
    driverName("<اسم السائق>"),
    driverPhone("<رقم السائق>"),
    hubName(
        "<اسم الفرع>"
    ),
    company("<اسم الشركة>"),
    storeName("<اسم المتجر>"),
    shareLocationUrl("<رابط مشاركة الموقع>"),
    postponeDate(
        "<تاريخ التأجيل>"
    ),
    expectedDeliveryDate("<تاريخ التوصيل المتوقع>"), cod("<التحصيل>");
}
