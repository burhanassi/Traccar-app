package com.logestechs.driver.utils

import com.google.gson.Gson
import com.logestechs.driver.api.responses.ChangeWorkLogStatusResponse
import com.logestechs.driver.api.responses.GetDriverCompanySettingsResponse
import com.logestechs.driver.api.responses.LoginResponse
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

        //Driver Company Settings
        fun saveSelectedServerIp(selectedServerIp: String) {
            prefs.push(SharedPrefsKeys.SELECTED_SERVER_IP.value, selectedServerIp)
        }

        fun getSelectedServerIp(): String {
            return prefs.pull(SharedPrefsKeys.SELECTED_SERVER_IP.value, "")
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
    SELECTED_SERVER_IP("selected_server_ip")
}