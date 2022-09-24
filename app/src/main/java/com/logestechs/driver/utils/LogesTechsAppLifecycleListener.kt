package com.logestechs.driver.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LogesTechsAppLifecycleListener : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        Executors.newSingleThreadScheduledExecutor().schedule({
            LogesTechsApp.isInBackground = false
        }, 2, TimeUnit.SECONDS)
    }

    override fun onStop(owner: LifecycleOwner) {
        LogesTechsApp.isInBackground = true
    }
}