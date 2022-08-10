package com.logestechs.driver.ui.splash

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

class SplashActivity : LogesTechsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        hideStatusBar()
        navigateIntoApp()
    }

    private fun navigateIntoApp() {
        Handler().postDelayed({
            var mIntent: Intent? = null
            mIntent = if (SharedPreferenceWrapper.getLoginResponse() != null) {
                getFailureReasons()
                Intent(this, DashboardActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(mIntent)
            finish()
        }, AppConstants.SPLASH_TIME_OUT)
    }

    private fun getFailureReasons() {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getFailureReasons()
                    if (response?.isSuccessful == true && response.body() != null) {
                        val data = response.body()
                        withContext(Dispatchers.Main) {
                            SharedPreferenceWrapper.saveFailureReasons(data)
                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                }
            }
        }
    }

}