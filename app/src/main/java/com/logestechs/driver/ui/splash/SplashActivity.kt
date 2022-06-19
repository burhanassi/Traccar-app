package com.logestechs.driver.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.logestechs.driver.R
import com.logestechs.driver.ui.login.LoginActivity
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.LogesTechsActivity

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
            mIntent = Intent(this, LoginActivity::class.java)
            startActivity(mIntent)
            finish()

        }, AppConstants.SPLASH_TIME_OUT)
    }

}