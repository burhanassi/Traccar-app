package com.logestechs.driver.utils

import android.os.Handler
import android.os.Looper


class PeriodicTask(action: Runnable?, interval: Long) {
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var mStatusChecker: Runnable? = null

    init {
        mStatusChecker = object : Runnable {
            override fun run() {
                action?.run()
                mHandler.postDelayed(this, interval)
            }
        }
    }

    @Synchronized
    fun startUpdates() {
        mStatusChecker!!.run()
    }

    @Synchronized
    fun stopUpdates() {
        if (mStatusChecker != null) {
            mHandler.removeCallbacks(mStatusChecker!!)
        }
    }
}