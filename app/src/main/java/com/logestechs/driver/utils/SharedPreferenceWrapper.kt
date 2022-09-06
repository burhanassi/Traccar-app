package com.logestechs.driver.utils

import com.google.gson.Gson
import com.logestechs.driver.api.responses.GetDriverCompanySettingsResponse
import com.logestechs.driver.api.responses.LoginResponse
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

        fun clearData() {
            val keys = enumValues<SharedPrefsKeys>()
            for (key in keys) {
                prefs.remove(key.value)
            }
        }
    }
}


private enum class SharedPrefsKeys(val value: String) {
    LOGIN_RESPONSE("login_response"),
    UUID_KEY("uuid_key"),
    DRIVER_COMPANY_SETTINGS_KEY("driver_company_settings_key")
}