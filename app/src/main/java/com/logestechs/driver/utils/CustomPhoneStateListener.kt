package com.logestechs.driver.utils

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
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
                    val callEndTime = Date()
                    val callDuration = callEndTime.time - callStartTime!!.time
                    Log.d("callDuration", callDuration.toString())
                    // Do something with call duration (e.g., save it)
                    // You can use callDuration to get the call duration in milliseconds
                    callStartTime = null
                    isOutgoingCall = false
                }
            }
        }
    }
}
