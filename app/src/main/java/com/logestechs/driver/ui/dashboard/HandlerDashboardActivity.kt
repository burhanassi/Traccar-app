package com.logestechs.driver.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.LogExceptionRequestBody
import com.logestechs.driver.data.model.Device
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.databinding.ActivityHandlerDashboardBinding
import com.logestechs.driver.ui.barcodeScanner.BarcodeScannerActivity
import com.logestechs.driver.ui.driverPackagesByStatusViewPager.DriverPackagesByStatusViewPagerActivity
import com.logestechs.driver.ui.findPackagesActivity.FindPackagesActivity
import com.logestechs.driver.ui.profile.ProfileActivity
import com.logestechs.driver.ui.sortOnShelveActivity.SortOnShelveActivity
import com.logestechs.driver.ui.unloadContainerActivity.UnloadContainerActivity
import com.logestechs.driver.ui.unloadFromCustomerActivity.UnloadFromCustomerActivity
import com.logestechs.driver.utils.*
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class HandlerDashboardActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityHandlerDashboardBinding
    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()
    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandlerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Helper.handleScanWay(this)
        initData()
        initOnClickListeners()
        handleNotificationToken()
        getNotificationsFirstTime()
    }

    override fun onResume() {
        super.onResume()
        if (Lingver.getInstance().getLocale().toString() != super.currentLangCode) {
            recreate()
        }
    }

    private fun initData() {
        binding.textDriverName.text =
            "${loginResponse?.user?.firstName} ${loginResponse?.user?.lastName}"

        if (companyConfigurations?.isDriverPickupPackagesByScanDisabled == true) {
//            binding.dashEntryScanPackages.root.visibility = View.GONE
        }

        if (Helper.isLogesTechsDriver()) {
            binding.containerLogestechsLogoBottom.visibility = View.VISIBLE
        } else {
            binding.containerLogestechsLogoBottom.visibility = View.GONE
        }
    }

    private fun initOnClickListeners() {
        binding.imageViewDriverLogo.setOnClickListener(this)
//        binding.dashEntryAcceptedPackages.root.setOnClickListener(this)
//        binding.dashEntryScanPackages.root.setOnClickListener(this)
        binding.dashEntrySortOnShelf.root.setOnClickListener(this)
        binding.dashEntryUnloadFromCustomer.root.setOnClickListener(this)
        binding.dashEntryUnloadContainer.root.setOnClickListener(this)
        binding.dashEntryFindPackages.root.setOnClickListener(this)
    }


    private fun handleNotificationToken() {
        val extras = intent.extras
        if (extras != null) {
            if (extras.getBoolean(BundleKeys.IS_LOGIN.name, false)) {
                if (loginResponse != null) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }

                        val device = loginResponse.device
                        if (device != null) {
                            device.notificationToken = task.result
                            callResetNotificationToken(device)
                        }
                    })
                }
            }
        }
    }

    //:- Action Handlers
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.image_view_driver_logo -> {
                val mIntent = Intent(this, ProfileActivity::class.java)
                startActivity(mIntent)
            }

//            R.id.dash_entry_accepted_packages -> {
//                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
//                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 1)
//                startActivity(mIntent)
//            }
//
//
//            R.id.dash_entry_scan_packages -> {
//                val mIntent = Intent(this, BarcodeScannerActivity::class.java)
//                startActivity(mIntent)
//            }

            R.id.dash_entry_sort_on_shelf -> {
                val mIntent = Intent(this, SortOnShelveActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_unload_from_customer -> {
                val mIntent = Intent(this, UnloadFromCustomerActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_unload_container -> {
                val mIntent = Intent(this, UnloadContainerActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_find_packages -> {
                val mIntent = Intent(this, FindPackagesActivity::class.java)
                startActivity(mIntent)
            }
        }
    }

    private fun callResetNotificationToken(device: Device) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = ApiAdapter.apiClient.resetNotificationToken(device)
                if (response!!.code() != 200 && !BuildConfig.DEBUG) {
                    ApiAdapter.apiClient.logException(
                        LogExceptionRequestBody(
                            "Notification Token",
                            device.notificationToken!!,
                            "",
                            "Android",
                            Helper.getDeviceInfo()
                        )
                    )
                }
            } catch (e: Exception) {
                Helper.logException(e, Throwable().stackTraceToString())
            }
        }
    }

    private fun getNotificationsFirstTime() {
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getNotifications()
                    if (response!!.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        SharedPreferenceWrapper.saveNotificationsCount(data.unReadUserNotificationsNo.toString())
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