package com.logestechs.driver.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.LoginRequestBody
import com.logestechs.driver.data.model.Device
import com.logestechs.driver.databinding.ActivityLoginBinding
import com.logestechs.driver.ui.dashboard.DashboardActivity
import com.logestechs.driver.utils.*
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import maes.tech.intentanim.CustomIntent
import org.json.JSONObject
import java.util.*

class LoginActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonLogin.setOnClickListener(this)
        binding.imageViewLanguage.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {

            R.id.imageView_language -> handleLanguage()
            R.id.button_login -> {
                var mIntent: Intent? = null
                mIntent = Intent(this, DashboardActivity::class.java)

                startActivity(mIntent)
                if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                    CustomIntent.customType(this, IntentAnimation.RTL.value)
                } else {
                    CustomIntent.customType(this, IntentAnimation.LTR.value)
                }
                finish()
            }
        }
    }

    private fun navigateFromLoginToSummary() {
        val mIntent = Intent(this, DashboardActivity::class.java)
        startActivity(mIntent)
        if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
            CustomIntent.customType(this, IntentAnimation.RTL.value)
        } else {
            CustomIntent.customType(this, IntentAnimation.LTR.value)
        }
        finish()
    }

    private fun handleLanguage() {
        if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
            Lingver.getInstance().setLocale(this, AppLanguages.ENGLISH.value)
            val intent = Intent(
                this,
                LoginActivity::class.java
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            Lingver.getInstance().setLocale(this, AppLanguages.ARABIC.value)

            val intent = Intent(
                this,
                LoginActivity::class.java
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (binding.etCompanyName.getText().isEmpty()) {
            binding.etCompanyName.makeInvalid()
            isValid = false
        } else {
            binding.etCompanyName.makeValid()
        }

        if (binding.etEmail.getText().isEmpty()) {
            binding.etEmail.makeInvalid()
            isValid = false
        } else {
            binding.etEmail.makeValid()
        }

        if (binding.etPassword.getText().isEmpty()) {
            binding.etPassword.makeInvalid()
            isValid = false
        } else {
            binding.etPassword.makeValid()
        }
        return isValid
    }


    //APIs
    private fun callLoginApi() {
        var uuid = SharedPreferenceWrapper.getUUID()

        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString()
            SharedPreferenceWrapper.saveUUID(uuid)
        }
        val loginRequestBody = LoginRequestBody(
            binding.etEmail.getText(),
            binding.etPassword.getText(),
            binding.etCompanyName.getText(),
            Device(uuid, "ANDROID"),
        )
        showWaitDialog()
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.login(loginRequestBody)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()!!
                        SharedPreferenceWrapper.saveLoginResponse(body)
                        withContext(Dispatchers.Main) {
                            navigateFromLoginToSummary()
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()?.string() ?: "")
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@LoginActivity,
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@LoginActivity,
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    hideWaitDialog()
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(this@LoginActivity, e.message)
                        } else {
                            Helper.showErrorMessage(this@LoginActivity, e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                this, getString(R.string.error_check_internet_connection)
            )
        }
    }

}