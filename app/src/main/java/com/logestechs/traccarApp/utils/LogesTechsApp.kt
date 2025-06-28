package com.logestechs.traccarApp.utils

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.cioccarellia.ksprefs.KsPrefs
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.logestechs.traccarApp.BuildConfig
import com.yariksoffice.lingver.Lingver
import java.lang.ref.WeakReference

class LogesTechsApp : Application() {

    var currentActivity: WeakReference<Activity?>? = null

    private val lifecycleListener: LogesTechsAppLifecycleListener by lazy {
        LogesTechsAppLifecycleListener()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupLifecycleListener()
        Lingver.init(instance, AppLanguages.ARABIC.value)
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCustomKey("is_production", "false")
        } else {
            FirebaseCrashlytics.getInstance().setCustomKey("is_production", "true")
        }
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleListener)
    }

    companion object {
        lateinit var instance: LogesTechsApp
        var isInBackground: Boolean = false
        val prefs by lazy { KsPrefs(instance) }
    }
}