package com.logestechs.driver.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.LogExceptionRequestBody
import com.logestechs.driver.data.model.Device
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.databinding.ActivityFulfilmentSorterDashboardBinding
import com.logestechs.driver.ui.arrivedShippingPlans.ArrivedShippingPlansActivity
import com.logestechs.driver.ui.barcodeScanner.FulfilmentSorterBarcodeScannerActivity
import com.logestechs.driver.ui.barcodeScanner.FulfilmentSorterScanMode
import com.logestechs.driver.ui.barcodeScanner.ShippingPlanBarcodeScanner
import com.logestechs.driver.ui.newFulfilmentOrders.NewFulfilmentOrdersActivity
import com.logestechs.driver.ui.pickedFulfilmentOrdersActivity.PickedFulfilmentOrdersActivity
import com.logestechs.driver.ui.profile.ProfileActivity
import com.logestechs.driver.ui.trackInventoryItemsActivity.TrackInventoryItemActivity
import com.logestechs.driver.utils.*
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FulfilmentSorterDashboardActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFulfilmentSorterDashboardBinding
    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()
    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFulfilmentSorterDashboardBinding.inflate(layoutInflater)
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

        if (Helper.isLogesTechsDriver()) {
            binding.containerLogestechsLogoBottom.visibility = View.VISIBLE
        } else {
            binding.containerLogestechsLogoBottom.visibility = View.GONE
        }
    }

    private fun initOnClickListeners() {
        binding.imageViewDriverLogo.setOnClickListener(this)
        binding.dashEntryReceiveShippingPlan.root.setOnClickListener(this)
        binding.dashEntryArrivedShippingPlans.root.setOnClickListener(this)
        binding.dashEntryPickedFulfilmentOrders.root.setOnClickListener(this)
        binding.dashEntrySortItemsIntoBins.root.setOnClickListener(this)
        binding.dashEntrySortBinsIntoLocations.root.setOnClickListener(this)
        binding.dashEntryNewFulfilmentOrders.root.setOnClickListener(this)
        binding.dashEntryPickedFulfilmentOrders.root.setOnClickListener(this)
        binding.dashEntryTrackInventoryItems.root.setOnClickListener(this)
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

            R.id.dash_entry_receive_shipping_plan -> {
                val mIntent = Intent(this, ShippingPlanBarcodeScanner::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_arrived_shipping_plans -> {
                val mIntent = Intent(this, ArrivedShippingPlansActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_sort_bins_into_locations -> {
                val mIntent = Intent(this, FulfilmentSorterBarcodeScannerActivity::class.java)
                mIntent.putExtra(
                    IntentExtrasKeys.FULFILMENT_SORTER_SCAN_MODE.name,
                    FulfilmentSorterScanMode.LOCATION
                )
                startActivity(mIntent)
            }


            R.id.dash_entry_sort_items_into_bins -> {
                val mIntent = Intent(this, FulfilmentSorterBarcodeScannerActivity::class.java)
                mIntent.putExtra(
                    IntentExtrasKeys.FULFILMENT_SORTER_SCAN_MODE.name,
                    FulfilmentSorterScanMode.SHIPPING_PLAN
                )
                startActivity(mIntent)
            }

            R.id.dash_entry_new_fulfilment_orders -> {
                val mIntent = Intent(this, NewFulfilmentOrdersActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_picked_fulfilment_orders -> {
                val mIntent = Intent(this, PickedFulfilmentOrdersActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_track_inventory_items -> {
                val mIntent = Intent(this, TrackInventoryItemActivity::class.java)
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