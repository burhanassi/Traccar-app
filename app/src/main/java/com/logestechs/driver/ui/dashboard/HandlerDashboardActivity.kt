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
import com.logestechs.driver.ui.profile.ProfileActivity
import com.logestechs.driver.ui.sortOnShelveActivity.SortOnShelveActivity
import com.logestechs.driver.utils.*
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class HandlerDashboardActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityHandlerDashboardBinding
    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()
    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandlerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initOnClickListeners()
        handleNotificationToken()
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

}