package com.logestechs.driver.utils

import com.google.gson.Gson
import com.logestechs.driver.api.responses.ChangeWorkLogStatusResponse
import com.logestechs.driver.api.responses.GetDriverCompanySettingsResponse
import com.logestechs.driver.api.responses.LoginResponse
import com.logestechs.driver.data.model.CompanyInfo
import com.logestechs.driver.data.model.LatLng
import com.logestechs.driver.utils.LogesTechsApp.Companion.prefs

class SharedPreferenceWrapper {
    companion object {
        //login response
        fun saveLoginResponse(loginResponse: LoginResponse?) {
            if (loginResponse != null) {
                val json = Gson().toJson(loginResponse)
                prefs.push(SharedPrefsKeys.LOGIN_RESPONSE.value, json)
            }
        }

        fun getLoginResponse(): LoginResponse? {
            val json = prefs.pull(SharedPrefsKeys.LOGIN_RESPONSE.value, "")

            return if (json.isEmpty()) {
                return null
            } else {
                Gson().fromJson(json, LoginResponse::class.java)
            }
        }

        fun deleteLoginResponse() {
            prefs.push(SharedPrefsKeys.LOGIN_RESPONSE.value, "")
        }

        //UUID
        fun saveUUID(value: String) {
            prefs.push(SharedPrefsKeys.UUID_KEY.value, value)
        }

        fun getUUID(): String {
            return prefs.pull(SharedPrefsKeys.UUID_KEY.value, "")
        }

        //Driver Company Settings
        fun saveDriverCompanySettings(getDriverCompanySettingsResponse: GetDriverCompanySettingsResponse?) {
            if (getDriverCompanySettingsResponse != null) {
                val json = Gson().toJson(getDriverCompanySettingsResponse)
                prefs.push(SharedPrefsKeys.DRIVER_COMPANY_SETTINGS_KEY.value, json)
            }
        }

        fun getDriverCompanySettings(): GetDriverCompanySettingsResponse? {
            val json = prefs.pull(SharedPrefsKeys.DRIVER_COMPANY_SETTINGS_KEY.value, "")

            return if (json.isEmpty()) {
                return null
            } else {
                Gson().fromJson(json, GetDriverCompanySettingsResponse::class.java)
            }
        }

        fun deleteDriverCompanySettings() {
            prefs.push(SharedPrefsKeys.DRIVER_COMPANY_SETTINGS_KEY.value, "")
        }

        //company info
        fun saveDriverCompanyInfo(companyInfo: CompanyInfo?) {
            if (companyInfo != null) {
                val json = Gson().toJson(companyInfo)
                prefs.push(SharedPrefsKeys.DRIVER_COMPANY_INFO.value, json)
            }
        }

        fun getDriverCompanyInfo(): CompanyInfo? {
            val json = prefs.pull(SharedPrefsKeys.DRIVER_COMPANY_INFO.value, "")

            return if (json.isEmpty()) {
                return null
            } else {
                Gson().fromJson(json, CompanyInfo::class.java)
            }
        }

        //work log id
        fun saveWorkLogId(getDriverCompanySettingsResponse: ChangeWorkLogStatusResponse?) {
            if (getDriverCompanySettingsResponse != null) {
                val json = Gson().toJson(getDriverCompanySettingsResponse)
                prefs.push(SharedPrefsKeys.WORK_LOG_ID_KEY.value, json)
            }
        }

        fun getWorkLogId(): ChangeWorkLogStatusResponse? {
            val json = prefs.pull(SharedPrefsKeys.WORK_LOG_ID_KEY.value, "")

            return if (json.isEmpty()) {
                return null
            } else {
                Gson().fromJson(json, ChangeWorkLogStatusResponse::class.java)
            }
        }

        //Last sync location
        fun saveLastSyncLocation(location: LatLng?) {
            if (location != null) {
                val json = Gson().toJson(location)
                prefs.push(SharedPrefsKeys.LAST_SYNC_LOCATION_KEY.value, json)
            }
        }

        fun getLastSyncLocation(): LatLng? {
            val json = prefs.pull(SharedPrefsKeys.LAST_SYNC_LOCATION_KEY.value, "")
            return if (json.isEmpty()) {
                return null
            } else {
                Gson().fromJson(json, LatLng::class.java)
            }
        }

        fun clearData() {
            val keys = enumValues<SharedPrefsKeys>()
            for (key in keys) {
                if (key != SharedPrefsKeys.IS_WHATSAPP_BUSINESS) {
                    prefs.remove(key.value)
                }
            }
        }

        fun saveIsWhatsappBusiness(isWhatsappBusiness: Boolean?) {
            prefs.push(SharedPrefsKeys.IS_WHATSAPP_BUSINESS.value, isWhatsappBusiness ?: false)
        }

        fun getIsWhatsappBusiness(): Boolean {
            return prefs.pull(SharedPrefsKeys.IS_WHATSAPP_BUSINESS.value, false)
        }

        fun saveSubpackagesQuantity(quantity: Int?) {
            prefs.push(SharedPrefsKeys.QUANTITY.value, quantity ?: 0)
        }

        fun getSubpackagesQuantity(): Int {
            return prefs.pull(SharedPrefsKeys.QUANTITY.value, 0)
        }

        //partially delivered
        fun saveIsPartiallyDelivered(isPartiallyDelivered: Boolean) {
            prefs.push(SharedPrefsKeys.IS_PARTIALLY_DELIVERED.value, isPartiallyDelivered)
        }

        fun getIsPartiallyDelivered(): Boolean {
            return prefs.pull(SharedPrefsKeys.IS_PARTIALLY_DELIVERED.value, false)
        }

        fun resetIsPartiallyDelivered() {
            prefs.push(SharedPrefsKeys.IS_PARTIALLY_DELIVERED.value, false)
        }

        //Driver Company Settings
        fun saveSelectedServerIp(selectedServerIp: String) {
            prefs.push(SharedPrefsKeys.SELECTED_SERVER_IP.value, selectedServerIp)
        }

        fun getSelectedServerIp(): String {
            return prefs.pull(SharedPrefsKeys.SELECTED_SERVER_IP.value, "")
        }

        //Driver Company Settings
        fun saveVideoUrl(url: String) {
            prefs.push(SharedPrefsKeys.VIDEO_URL.value, url)
        }

        fun getVideoUrl(): String {
            return prefs.pull(SharedPrefsKeys.VIDEO_URL.value, "")
        }

        //Notifications Count
        fun saveNotificationsCount(notificationsCount: String) {
            prefs.push(SharedPrefsKeys.NOTIFICATIONS_COUNT.value, notificationsCount)
        }

        fun getNotificationsCount(): String {
            return prefs.pull(SharedPrefsKeys.NOTIFICATIONS_COUNT.value, "")
        }

        //Scan Way
        fun saveScanWay(scanWay: String) {
            prefs.push(SharedPrefsKeys.SCAN_WAY.value, scanWay)
        }

        fun getScanWay(): String {
            return prefs.pull(SharedPrefsKeys.SCAN_WAY.value, "")
        }

        fun saveInvoiceForDeeplink(invoiceNumber: String) {
            val json = Gson().toJson(invoiceNumber)
            prefs.push(SharedPrefsKeys.INVOICE_NUMBER_FOR_DEEP_LINK.value, json)
        }
        fun getInvoiceForDeeplink(): String? {
            val json = prefs.pull(SharedPrefsKeys.INVOICE_NUMBER_FOR_DEEP_LINK.value, "")
            return if (json.isEmpty()) {
                null
            } else {
                Gson().fromJson(json, String::class.java)
            }
        }
        fun deleteInvoiceForDeeplink() {
            prefs.push(SharedPrefsKeys.INVOICE_NUMBER_FOR_DEEP_LINK.value, "")
        }
        fun doesInvoiceForDeeplinkExist(): Boolean{
            val json = prefs.pull(SharedPrefsKeys.INVOICE_NUMBER_FOR_DEEP_LINK.value, "")
            return json.isNotEmpty()
        }
    }
}


private enum class SharedPrefsKeys(val value: String) {
    LOGIN_RESPONSE("login_response"),
    UUID_KEY("uuid_key"),
    DRIVER_COMPANY_SETTINGS_KEY("driver_company_settings_key"),
    LAST_SYNC_LOCATION_KEY("last_sync_location_key"),
    WORK_LOG_ID_KEY("work_log_key_id"),
    IS_WHATSAPP_BUSINESS("is_whatsapp_business"),
    SELECTED_SERVER_IP("selected_server_ip"),
    NOTIFICATIONS_COUNT("notifications_count"),
    SCAN_WAY("scan_way"),
    INVOICE_NUMBER_FOR_DEEP_LINK("invoice_number_for_deep_link"),
    QUANTITY("quantity"),
    IS_PARTIALLY_DELIVERED("is_partially_delivered"),
    DRIVER_COMPANY_INFO("driver_company_info"),
    VIDEO_URL("video_url"),
}