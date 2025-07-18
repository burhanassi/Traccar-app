package com.logestechs.traccarApp.utils

import com.logestechs.traccarApp.BuildConfig

class AppConstants {
    companion object {
        const val SPLASH_TIME_OUT: Long = 2000
        const val DATE_PICKER_OPEN_DELAY: Long = 1000
        const val FAILURE_REASONS_UPDATE_INTERVAL: Long = 1

        const val PACKAGE_NAME = BuildConfig.APPLICATION_ID

        const val BASE_URL = BuildConfig.base_url
        const val BROADCAST_CREDENTIAL = "Broadcast_Credential"


        const val ERROR_KEY = "error"

        //Default Api Params
        const val DEFAULT_PAGE_SIZE = 20
        const val DEFAULT_PAGE = 1

        //request codes
        const val REQUEST_TAKE_PHOTO = 4001
        const val REQUEST_LOAD_PHOTO = 4002
        const val REQUEST_TAKE_VIDEO = 1003
        const val REQUEST_SCAN_BARCODE = 4003
        const val REQUEST_CAMERA_AND_STORAGE_PERMISSION = 5001
        const val REQUEST_STORAGE_PERMISSION = 5002
        const val REQUEST_CAMERA_PERMISSION = 5003
        const val REQUEST_LOCATION_PERMISSION = 5004
        const val REQUEST_SCAN_BUNDLE = 5005
        const val REQUEST_VERIFY_PACKAGE = 5006
        const val REQUEST_READ_PHONE_STATE = 123
        const val OPEN_SOFTPOS_RESULT_CODE = 1

        //permission codes
        const val PERMISSIONS_REQUEST_PHONE_CALL = 1
        const val PERMISSIONS_REQUEST_PHONE_CAMERA = 2
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3
        const val PERMISSIONS_REQUEST_SEND_SMS = 4

        const val IMAGE_FULL_QUALITY = 50

        const val SOFTPOS_PACKAGE_NAME = "com.interpaymea.softpos"
        const val SOFTPOS_CLASS_NAME = "com.interpaymea.softpos.MainActivity"
        const val WAZE_PACKAGE_NAME = "com.waze"

        const val PROLO_COMPANY_ID = 368
    }
}

enum class BundleKeys {
    NOTIFICATIONS_KEY, UNREAD_NOTIFICATIONS_COUNT, IS_LOGIN, COMPANY_INFO, PACKAGES_KEY, PACKAGES_COUNT, PKG_KEY
}

enum class AppLanguages(val value: String) {
    ARABIC("ar"),
    ENGLISH("en")
}

enum class DropdownTag {
    SIGN_UP_VILLAGES,
    LOCATIONS
}

enum class PhoneType {
    MOBILE, TELEPHONE
}

enum class IntentAnimation(val value: String) {
    RTL("right-to-left"),
    LTR("left-to-right")
}

enum class DateFormats(val value: String) {
    DEFAULT_FORMAT("yyyy-MM-dd"),
    SERVER_FORMAT("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ"),
    TRACK_NODE_DATE_FORMAT("yy-MM-dd"),
    TRACK_NODE_TIME_FORMAT("hh:mm aa"),
    NOTIFICATION_LIST_ITEM_FORMAT("dd-MM-yyyy, hh:mm aa"),
    NOTIFICATION_DAY_FORMAT("EEEE"),
    MESSAGE_TEMPLATE_WITH_TIME("dd-MM-yyyy HH:mm"),
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
    OMR("OMR"),
    EGP("EGP")
}

enum class CountriesCode(val value: String) {
    NIS("PS"),
    JOD("JO"),
    KWD("KW"),
    BHD("BH"),
    IQD("IQ"),
    SAR("SA"),
    EGP("EG"),
    TRY("TR"),
    OMR("OM"),
    LYD("LY"),
    USD("US"),
    QAR("QA"),
    AED("AE"),
    SYP("SY"),
    LBP("LB"),
    EUR("FR"),
    GBP("GB"),
    SDG("SD"),
    SSP("SS"),
    MAD("MA"),
    YER("YE"),
    DZD("DZ"),
    ARS("AR"),
    AUD("AU"),
    BRL("BR"),
    BGN("BG"),
    CAD("CA"),
    CNY("CN"),
    CYP("CY"),
    INR("IN"),
    MYR("MY"),
    MXN("MX"),
    SGD("SG"),
    SEK("SE"),
    CHF("CH"),
    TND("TN")
}

enum class DraftPickupStatus {
    ASSIGNED_TO_DRIVER,
    ACCEPTED_BY_DRIVER,
    IN_CAR
}

enum class IntentExtrasKeys() {
    SELECTED_PACKAGES_TAB,
    CUSTOMER_WITH_PACKAGES_FOR_PICKUP,
    SCAN_TYPE,
    SHIPPING_PLAN_BARCODE,
    SINGLE_SCAN_BARCODE_SCANNER_LISTENER,
    PACKAGE_TO_DELIVER,
    CUSTOMER_WITH_PACKAGES_TO_RETURN,
    CUSTOMER_WITH_BUNDLES_TO_RETURN,
    DRAFT_PICKUP,
    EXTRA_RECEIVED_NOTIFICATION,
    IN_CAR_PACKAGE_STATUS,
    MASS_COD_REPORT_TO_DELIVER,
    MASS_COD_REPORT_TO_DELIVER_ALL,
    SCANNED_BARCODE,
    FULFILMENT_SORTER_SCAN_MODE,
    FULFILMENT_PICKER_SCAN_MODE,
    FULFILMENT_ORDER,
    FULFILMENT_ORDERS,
    BUNDLE,
    DRIVER_PACKAGES_LOCATIONS,
    PICK_WITHOUT_TOTE,
    FULFILMENT_RETURN_ORDER_SCAN_MODE
}

enum class BarcodeScanType {
    PACKAGE_PICKUP, SHIPPING_PLAN_PICKUP
}

enum class PackageType {
    ALL,
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
    FAILED("FAILED"),
    TO_DELIVER_PICKUP("TO_DELIVER_PICKUP"),
    TO_DELIVER_DELIVERY("TO_DELIVER_DELIVERY")
}

enum class DeliveredPackageStatus(val value: String) {
    ALL("ALL"),
    PARTIALLY_DELIVERED("PARTIALLY_DELIVERED"),
    DELIVERED("DELIVERED")
}

enum class ReturnedPackageStatus(val value: String) {
    ALL("ALL"),
    PARTIALLY_DELIVERED("PARTIALLY_DELIVERED"),
    RETURNED("RETURNED"),
    EXCHANGE("EXCHANGE")
}


enum class MassCodReportsViewMode(val value: String) {
    BY_REPORT("BY_REPORT"),
    BY_CUSTOMER("BY_CUSTOMER")
}

enum class ConfirmationDialogAction {
    RETURN_PACKAGE,
    CLICKPAY_PAYMENT,
    CLICKPAY_RESULT,
    DELIVER_PACKAGE,
    PACKAGE_NOTE,
    DELIVER_TO_WAREHOUSE
}

enum class PaymentGatewayType {
    NEAR_PAY,
    INTER_PAY
}

enum class PaymentType(val englishLabel: String, val arabicLabel: String) {
    CASH("Cash", "نقداً"),
    CHEQUE("Cheque", "شيك"),
    BANK_TRANSFER("Bank Transfer", "حوالة بنكية"),
    PREPAID("Prepaid", "دفع مسبق"),
    DIGITAL_WALLET("Digital Wallet", "محفظة الكترونية"),
    CARD("Card Payment", "بطاقة ائتمانية"),
    INTER_PAY("InterPay","InterPay"),
    NEAR_PAY("NearPay","NearPay"),
    CLICK_PAY("ClickPay","ClickPay")
}

enum class DeliveryType {
    FULL,
    PARTIAL
}

enum class SmsTemplateTag(val arabicTag: String, val englishTag: String) {
    NAME("<الاسم>", "<Receiver Name>"),
    barcode("<باركود>", "<Barcode>"),
    driverName("<اسم السائق>", "<Driver Name>"),
    driverPhone("<رقم السائق>", "<Driver Phone>"),
    hubName("<اسم الفرع>", "<Hub Name>"),
    company("<اسم الشركة>", "<Company Name>"),
    storeName("<اسم المتجر>", "<Business Name>"),
    senderName("<اسم المرسل>", "<Sender Name>"),
    shareLocationUrl("<رابط مشاركة الموقع>", "<Sharing Location URL>"),
    postponeDate("<تاريخ التأجيل>", "<Date Postponed>"),
    expectedDeliveryDate("<تاريخ التوصيل المتوقع>", "<Expected Delivery Date>"),
    cod("<التحصيل>", "<COD>"),
    customerPhoneNumber("<رقم المتجر الاضافي>", "<Customer Second Phone Number>"),
    receiverAddress("<عنوان المستقبل>", "<Receiver Address>"),
    packageContent("<محتوى الطرد>", "<Package Content>"),
    receiverPhone("<رقم المستلم>", "<Receiver Phone>");
    companion object {
        fun replaceTags(template: String): String {
            var replacedTemplate = template
            for (tag in values()) {
                val tagRegex = Regex("(${tag.arabicTag})|(${tag.englishTag})")
                val matchedTag = tagRegex.find(replacedTemplate)
                if (matchedTag != null) {
                    val detectedTag = matchedTag.groupValues[1] // Arabic tag is in group 1
                    val replacedTag = if (detectedTag == tag.arabicTag) tag.name else tag.englishTag
                    replacedTemplate = replacedTemplate.replace(detectedTag, replacedTag)
                }
            }
            return replacedTemplate
        }
    }
}


enum class AdminPackageStatus(
    val english: String,
    val arabic: String
) {
    DRAFT("Draft", "مسودة"),
    PENDING_CUSTOMER_CARE_APPROVAL("Submitted", "طلب جديد"),
    APPROVED_BY_CUSTOMER_CARE_AND_WAITING_FOR_DISPATCHER(
        "Ready for dispatching",
        "بانتظار تعيين السائق"
    ),
    CANCELLED("Cancelled", "ملغاة"),
    ASSIGNED_TO_DRIVER_AND_PENDING_APPROVAL("Assigned to Drivers", "بإنتظار موافقة السائق"),
    REJECTED_BY_DRIVER_AND_PENDING_MANGEMENT("Rejected By Drivers", "رفضها السائق"),
    ACCEPTED_BY_DRIVER_AND_PENDING_PICKUP("Pending Pickup", "بإنتظار التحميل"),
    SCANNED_BY_DRIVER_AND_IN_CAR("Picked", "في المركبة"),
    SCANNED_BY_HANDLER_AND_UNLOADED("Pending Sorting", "بإنتظار التصنيف"),
    MOVED_TO_SHELF_AND_OUT_OF_HANDLER_CUSTODY("Sorted on Shelves", "على الرفوف"),
    OPENED_ISSUE_AND_WAITING_FOR_MANAGEMENT(
        "Reported to Management",
        "بإنتظار مراجعة الادارة"
    ),
    DELIVERED_TO_RECIPIENT("Delivered", "تم توصيلها"),
    POSTPONED_DELIVERY("Postponed Delivery", "مؤجلة لوقت آخر"),
    RETURNED_BY_RECIPIENT("Returned by Recipient", "تم إرجاعها"),
    DELAYED("Delayed", "متأخرة"),
    COD_RECEIVED_BY_ACCOUNTANT("Received by Accountant", "مستلمة من المحاسب"),
    COD_SORTED_BY_ACCOUNTANT("Sorted by Accountant", "مفرزة من المحاسب"),
    COD_OUT_OF_ACCOUNTANT_CUSTODY("Out of Accountant Custody", "خارج وصاية المحاسب"),
    COMPLETED("Completed", "مغلقة"),
    FAILED("Failed", "عالق"),
    TRANSFERRED_OUT("Transferred out", "مصدرة إلى شريك"),
    PARTIALLY_DELIVERED("Partially delivered", "تم تسليمها بشكل جزئي"),
    SWAPPED("Swapped", "تم تبديلها"),
    BROUGHT("Brought", "تم إحضارها")
}

enum class IntegrationSource {
    FULFILLMENT
}
enum class UserRole {
    DISPATCHER, DRIVER, CLERK, CUSTOMER_CARE, HANDLER, STOCKING_AND_PACKING_EMPLOYEE
}

enum class DeliveryAttemptType {
    WHATSAPP_SMS, PHONE_SMS, PHONE_CALL
}


enum class ShippingPlanStatus {
    AWAITING_PICKUP,
    ASSIGNED_TO_DRIVER,
    PICKED_UP,
    ARRIVED_AT_DESTINATION,
    PARTIALLY_RECEIVED,
    RECEIVED,
    REJECTED_ITEMS
}

enum class FulfilmentOrderStatus {
    CREATED,
    PICKED,
    PARTIALLY_PICKED,
    PACKED,
    DELIVERED,
    CANCELED,
    RETURNED
}

enum class VerificationStatus {
    SENT, VERIFIED, NOT_SENT
}

enum class FulfillmentOrderPackagingType {
    BY_CUSTOMER, BY_WAREHOUSE
}

enum class ProductItemRejectReasonKey(val englishLabel: String, val arabicLabel: String) {
    DAMAGED("Damaged", "تالف"),
    WRONG_COLOR("Wrong color", "لون غير مطابق"),
    WRONG_ITEM("Wrong Item", "عنصر غير مطابق"),
    WRONG_SKU("Wrong SKU", "SKU غير مطابق"),
}

enum class FulfillmentItemStatus(val english: String, val arabic: String) {
    SORTED("Sorted", "تم تصنيفها"),
    REJECTED("Rejected", "مرفوضة"),
    UNSORTED("Unsorted", "غير مصنفة"),
    PICKED("Picked", "محملة"),
    PACKED("Packed", "مغلفة"),
    DAMAGED("Damaged", "تالف"),
    RETURNED("Returned", "مرجعة")
}

enum class ReturnedFulfillmentOrderStatus(val english: String, val arabic: String) {
    RETURNED("Returned", "راجع"),
    PARTIALLY_DELIVERED("Partially Delivered", "تم توصيلها بشكل جزئي"),
    CANCELED("Cancelled", "ملغاة");

    companion object {
        fun fromStatus(status: String?): ReturnedFulfillmentOrderStatus? {
            return values().find { it.name == status }
        }
    }
}