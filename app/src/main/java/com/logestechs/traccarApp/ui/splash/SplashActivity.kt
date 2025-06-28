package com.logestechs.traccarApp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.logestechs.traccarApp.R
import com.logestechs.traccarApp.api.ApiAdapter
import com.logestechs.traccarApp.ui.dashboard.DriverDashboardActivity
import com.logestechs.traccarApp.ui.registerDevice.RegisterDeviceActivity
import com.logestechs.traccarApp.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : LogesTechsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        hideStatusBar()
        navigateIntoApp()
    }

    private fun navigateIntoApp() {
        Handler().postDelayed({
            callLoginApi()
        }, AppConstants.SPLASH_TIME_OUT)
    }

    private fun navigateFromSplashToRegister() {
        startActivity(
            Intent(
                super.getContext(),
                RegisterDeviceActivity::class.java
            )
        )
        finish()
    }

    private fun navigateFromSplashToDashboard() {
        startActivity(
            Intent(
                super.getContext(),
                DriverDashboardActivity::class.java
            )
        )
        finish()
    }

    private fun callLoginApi() {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.login("admin", "admin")
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if (SharedPreferenceWrapper.getUUID() != ""){
                                navigateFromSplashToDashboard()
                            } else {
                                navigateFromSplashToRegister()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                }
            }
        }
    }
}