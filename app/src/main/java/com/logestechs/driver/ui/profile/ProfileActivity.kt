package com.logestechs.driver.ui.profile

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.ModifyProfileRequestBody
import com.logestechs.driver.databinding.ActivityProfileBinding
import com.logestechs.driver.ui.login.LoginActivity
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.dialogs.ChangeProfileDialog
import com.logestechs.driver.utils.interfaces.ChangeProfileDialogListener
import com.logestechs.driver.utils.location.AlarmReceiver
import com.logestechs.driver.utils.location.MyLocationService
import com.squareup.picasso.Picasso
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ProfileActivity : LogesTechsActivity(), View.OnClickListener, ChangeProfileDialogListener {
    private lateinit var binding: ActivityProfileBinding

    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initListeners()
    }

    private fun initData() {
        binding.textDriverName.text =
            "${loginResponse?.user?.firstName} ${loginResponse?.user?.lastName}"
        binding.textUserEmail.text = loginResponse?.user?.email
        binding.textMobileNumber.text = loginResponse?.user?.phone
        binding.switchIsWhatsappBusiness.isChecked = SharedPreferenceWrapper.getIsWhatsappBusiness()
        Picasso.get().load(loginResponse?.user?.barcodeImage).into(binding.imageDriverBarcode)
        binding.driverIdLabel.text = loginResponse?.user?.id.toString()
        binding.categoryLabel.text = loginResponse?.user?.driverCategoryName
    }

    private fun initListeners() {
        binding.buttonChangeLanguage.setOnClickListener(this)
        binding.buttonEditProfile.setOnClickListener(this)
        binding.buttonEditPassword.setOnClickListener(this)
        binding.buttonLogout.setOnClickListener(this)
        binding.switchIsWhatsappBusiness.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferenceWrapper.saveIsWhatsappBusiness(isChecked)
        }
    }

    //apis
    private fun callGetDriverCompanySettings() {
        showWaitDialog()
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
                            val intent = Intent(
                                super.getContext(),
                                ProfileActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()?.string() ?: "")
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }
                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }


    private fun callLogout() {
        showWaitDialog()
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.logout(SharedPreferenceWrapper.getWorkLogId()?.id)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    // Check if response was successful.
                    if (response!!.isSuccessful && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            SharedPreferenceWrapper.clearData()
                            val intent = Intent(
                                super.getContext(),
                                LoginActivity::class.java
                            )
                            cancelLocationSync()
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()?.string() ?: "")
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }
                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun callChangeProfile(body: ModifyProfileRequestBody, isPhone: Boolean) {
        showWaitDialog()
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.changeProfile(body)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    // Check if response was successful.
                    if (response!!.isSuccessful && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if (isPhone){
                                loginResponse?.user?.phone = body.phone
                                SharedPreferenceWrapper.saveLoginResponse(loginResponse)
                                binding.textMobileNumber.text = body.phone
                                Helper.showSuccessMessage(
                                    super.getContext(),
                                    getString(R.string.success_operation_completed)
                                )
                            } else {
                                callLogout()
                            }
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()?.string() ?: "")
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }
                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_change_language -> {
                if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                    Lingver.getInstance().setLocale(this, AppLanguages.ENGLISH.value)
                    callGetDriverCompanySettings()
                } else {
                    Lingver.getInstance().setLocale(this, AppLanguages.ARABIC.value)
                    callGetDriverCompanySettings()
                }
            }

            R.id.button_edit_profile -> {
                ChangeProfileDialog(super.getContext(), this, isPhone = true).showDialog()
            }

            R.id.button_edit_password -> {
                ChangeProfileDialog(super.getContext(), this, isPhone = false).showDialog()
            }

            R.id.button_logout -> {
                callLogout()
            }
        }
    }

    private fun cancelLocationSync() {
        val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
        val myIntent = Intent(this, AlarmReceiver::class.java)
        var pendingIntent: PendingIntent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            pendingIntent =
                PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        alarmManager.cancel(pendingIntent)
        val myService = Intent(this, MyLocationService::class.java)
        stopService(myService)
    }

    override fun onProfileChanged(profileChangeRequestBody: ModifyProfileRequestBody?, isPhone: Boolean) {
        callChangeProfile(profileChangeRequestBody!!, isPhone)
    }
}