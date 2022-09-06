package com.logestechs.driver.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.ui.dashboard.DashboardActivity
import com.logestechs.driver.ui.login.LoginActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
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
            if (SharedPreferenceWrapper.getLoginResponse() != null) {
                callGetDriverCompanySettings()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

        }, AppConstants.SPLASH_TIME_OUT)
    }

    private fun callGetDriverCompanySettings() {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDriverCompanySettings()
                    if (response?.isSuccessful == true && response.body() != null) {
                        val data = response.body()
                        withContext(Dispatchers.Main) {
                            SharedPreferenceWrapper.saveDriverCompanySettings(data)
                            startActivity(Intent(super.getContext(), DashboardActivity::class.java))
                            finish()
                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                }
            }
        }
    }
}