package com.logestechs.traccarApp.ui.registerDevice

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import com.google.android.gms.location.LocationServices
import com.logestechs.traccarApp.R
import com.logestechs.traccarApp.api.ApiAdapter
import com.logestechs.traccarApp.api.requests.DeviceRequest
import com.logestechs.traccarApp.databinding.ActivityRegisterDeviceBinding
import com.logestechs.traccarApp.ui.dashboard.DriverDashboardActivity
import com.logestechs.traccarApp.utils.AppConstants
import com.logestechs.traccarApp.utils.Helper
import com.logestechs.traccarApp.utils.LogesTechsActivity
import com.logestechs.traccarApp.utils.SharedPreferenceWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RegisterDeviceActivity: LogesTechsActivity(), View.OnClickListener {
    private lateinit var binding: ActivityRegisterDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initOnClickListeners()
    }

    private fun initOnClickListeners() {
        binding.btnRegister.setOnClickListener(this)
    }

    //:- Action Handlers
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnRegister -> {
                registerDevice()
            }
        }
    }

    private fun navigateFromRegisterToDashboard() {
        startActivity(
            Intent(
                super.getContext(),
                DriverDashboardActivity::class.java
            )
        )
        finish()
    }
    //Apis
    private fun registerDevice() {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val request = DeviceRequest(binding.etDeviceName.text.toString(),
                        binding.etDeviceId.text.toString())
                    val response = ApiAdapter.apiClient.register(request)
                    if (response.isSuccessful && response.body() != null) {
                        SharedPreferenceWrapper.saveUUID(binding.etDeviceId.text.toString())
                        navigateFromRegisterToDashboard()
                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    super.getContext(),
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(super.getContext(), e.message)
                        } else {
                            Helper.showErrorMessage(super.getContext(), e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            Helper.showErrorMessage(
                this, getString(R.string.error_check_internet_connection)
            )
        }
    }
}