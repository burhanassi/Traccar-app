package com.logestechs.driver.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.logestechs.driver.BuildConfig
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
import org.json.JSONObject
import java.util.*

class LoginActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (BuildConfig.DEBUG) {
            binding.etCompanyName.editText.setText("Logestechs")
            binding.etEmail.editText.setText("driver@gmail.com")
            binding.etPassword.editText.setText("test123")
        }

        binding.buttonLogin.setOnClickListener(this)
        binding.imageViewLanguage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {

            R.id.imageView_language -> handleLanguage()
            R.id.button_login -> {
                if (validateInput()) {
                    if (binding.etEmail.getText().length >= 6) {
                        if (Helper.validatePassword(binding.etPassword.getText())) {
                            callLoginApi()
                        } else {
                            binding.etPassword.makeInvalid()
                            Helper.showErrorMessage(
                                this@LoginActivity,
                                getString(R.string.error_insert_valid_password)
                            )
                        }
                    } else {
                        binding.etEmail.makeInvalid()
                        Helper.showErrorMessage(
                            this@LoginActivity,
                            getString(R.string.error_insert_valid_email)
                        )
                    }
                } else {
                    Helper.showErrorMessage(
                        this@LoginActivity,
                        getString(R.string.error_fill_all_mandatory_fields)
                    )
                }
            }
        }
    }

    private fun navigateFromLoginToDashboard() {
        val mIntent = Intent(this, DashboardActivity::class.java)
        mIntent.putExtra(BundleKeys.IS_LOGIN.name, true);
        startActivity(mIntent)
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

        if (binding.etCompanyName.isEmpty()) {
            binding.etCompanyName.makeInvalid()
            isValid = false
        } else {
            binding.etCompanyName.makeValid()
        }

        if (binding.etEmail.isEmpty()) {
            binding.etEmail.makeInvalid()
            isValid = false
        } else {
            binding.etEmail.makeValid()
        }

        if (binding.etPassword.isEmpty()) {
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
                    if (response?.isSuccessful == true && response.body() != null) {
                        val body = response.body()
                        SharedPreferenceWrapper.saveLoginResponse(body)
                        withContext(Dispatchers.Main) {
                            callGetDriverCompanySettings()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            hideWaitDialog()
                        }
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

    private fun callGetDriverCompanySettings() {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDriverCompanySettings()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val data = response.body()
                        withContext(Dispatchers.Main) {
                            SharedPreferenceWrapper.saveDriverCompanySettings(data)
                            navigateFromLoginToDashboard()
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