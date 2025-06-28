package com.logestechs.traccarApp.utils

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import java.util.*

class CustomPhoneStateListener : PhoneStateListener() {

    private var callStartTime: Date? = null
    companion object {
        var isOutgoingCall = false
    }

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        super.onCallStateChanged(state, phoneNumber)

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                // Phone ringing
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Call started
                callStartTime = Date()
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // Call ended
                if (callStartTime != null && isOutgoingCall) {

                    callStartTime = null
                    isOutgoingCall = false
                }
            }
        }
    }
}
