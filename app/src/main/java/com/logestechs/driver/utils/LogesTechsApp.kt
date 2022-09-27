package com.logestechs.driver.utils

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.cioccarellia.ksprefs.KsPrefs
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.logestechs.driver.BuildConfig
import com.yariksoffice.lingver.Lingver
import java.lang.ref.WeakReference

class LogesTechsApp : Application() {

    var currentActivity: WeakReference<Activity?>? = null

    private val lifecycleListener: LogesTechsAppLifecycleListener by lazy {
        LogesTechsAppLifecycleListener()
    }

    override fun onCreate() {
        super.onCreate()
        setupLifecycleListener()
        instance = this
        Lingver.init(instance, AppLanguages.ARABIC.value)
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        FirebaseCrashlytics.getInstance()
            .setCustomKey("user_id", SharedPreferenceWrapper.getLoginResponse()?.user?.id ?: 0)
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCustomKey("is_production", "true")
        } else {
            FirebaseCrashlytics.getInstance().setCustomKey("is_production", "false")
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