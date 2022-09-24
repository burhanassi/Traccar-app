package com.logestechs.driver.ui.splash

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Device
import com.logestechs.driver.databinding.DialogForceUpdateBinding
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
    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        hideStatusBar()
        handleNotificationToken()
        getMinVersion("https://play.google.com/store/apps/details?id=com.logestechs.driver")
    }

    private fun navigateIntoApp() {
        Handler().postDelayed({
            if (loginResponse != null) {
                callGetDriverCompanySettings()
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

        }, AppConstants.SPLASH_TIME_OUT)
    }

    private fun handleNotificationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            if (loginResponse != null) {
                if (loginResponse.device != null) {
                    loginResponse.device.notificationToken = task.result
                    callResetNotificationToken(loginResponse.device)
                }
            }
        })
    }

    private fun showForceUpdateDialog(
        message: String = "",
        updateUrl: String
    ) {
        val dialogBuilder = AlertDialog.Builder(this, 0)
        val binding: DialogForceUpdateBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                this
            ), R.layout.dialog_force_update, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()

        binding.textMessage.text = message

        binding.buttonConfirm.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
            try {
                startActivity(browserIntent)
            } catch (e: java.lang.Exception) {
                Helper.showErrorMessage(
                    this,
                    getString(R.string.message_update_error)
                )
            }
        }

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
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

    private fun getMinVersion(updateUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = ApiAdapter.apiClient.getMinVersion()
                if (response!!.isSuccessful && response.body() != null) {
                    withContext(Dispatchers.Main) {
                        if (Helper.isMinVersionHigher(
                                response.body()!!.version,
                                this@SplashActivity
                            )
                        ) {
                            showForceUpdateDialog(
                                getString(R.string.message_update_app),
                                updateUrl
                            )
                        } else {
                            navigateIntoApp()
                        }
                    }

                }
            } catch (e: java.lang.Exception) {
                Helper.logException(e, Throwable().stackTraceToString())
            }
        }
    }

    private fun callResetNotificationToken(device: Device) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                ApiAdapter.apiClient.resetNotificationToken(device)
            } catch (e: java.lang.Exception) {
                Helper.logException(e, Throwable().stackTraceToString())
            }
        }
    }
}