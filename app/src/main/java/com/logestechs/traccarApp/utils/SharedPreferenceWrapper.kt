package com.logestechs.traccarApp.utils

import com.google.gson.Gson
import com.logestechs.traccarApp.data.model.LatLng
import com.logestechs.traccarApp.utils.LogesTechsApp.Companion.prefs

class SharedPreferenceWrapper {
    companion object {

        //UUID
        fun saveUUID(value: String) {
            prefs.push(SharedPrefsKeys.UUID_KEY.value, value)
        }

        fun getUUID(): String {
            return prefs.pull(SharedPrefsKeys.UUID_KEY.value, "")
        }

        fun deleteUUID() {
            prefs.push(SharedPrefsKeys.UUID_KEY.value, "")
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

        //Notifications Count
        fun saveNotificationsCount(notificationsCount: String) {
            prefs.push(SharedPrefsKeys.NOTIFICATIONS_COUNT.value, notificationsCount)
        }

        fun getNotificationsCount(): String {
            return prefs.pull(SharedPrefsKeys.NOTIFICATIONS_COUNT.value, "")
        }
    }
}


private enum class SharedPrefsKeys(val value: String) {
    UUID_KEY("uuid_key"),
    LAST_SYNC_LOCATION_KEY("last_sync_location_key"),
    NOTIFICATIONS_COUNT("notifications_count")
}