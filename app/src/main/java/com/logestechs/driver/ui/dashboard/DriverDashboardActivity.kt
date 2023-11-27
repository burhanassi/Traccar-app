package com.logestechs.driver.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.ChangeWorkLogStatusRequestBody
import com.logestechs.driver.api.requests.LogExceptionRequestBody
import com.logestechs.driver.api.requests.UpdateLocationRequestBody
import com.logestechs.driver.api.responses.GetDashboardInfoResponse
import com.logestechs.driver.data.model.Device
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.databinding.ActivityDriverDashboardBinding
import com.logestechs.driver.ui.barcodeScanner.BarcodeScannerActivity
import com.logestechs.driver.ui.broughtPackages.BroughtPackagesActivity
import com.logestechs.driver.ui.checkInActivity.CheckInActivity
import com.logestechs.driver.ui.driverDraftPickupsByStatusViewPager.DriverDraftPickupsByStatusViewPagerActivity
import com.logestechs.driver.ui.driverPackagesByStatusViewPager.DriverPackagesByStatusViewPagerActivity
import com.logestechs.driver.ui.driverRouteActivity.DriverRouteActivity
import com.logestechs.driver.ui.massCodReports.MassCodReportsActivity
import com.logestechs.driver.ui.profile.ProfileActivity
import com.logestechs.driver.ui.returnedPackages.ReturnedPackagesActivity
import com.logestechs.driver.ui.warehousePackagesByStatusViewPager.WarehousePackagesByStatusViewPagerActivity
import com.logestechs.driver.utils.*
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.location.AlarmReceiver
import com.logestechs.driver.utils.location.LocationListener
import com.logestechs.driver.utils.location.MyLocationService
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.sql.Timestamp
import java.util.*


class DriverDashboardActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDriverDashboardBinding
    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()
    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationManager: LocationManager? = null

    private var isInService: Boolean = false
    private var serviceStatusReferenceDate: Date? = null

    private val periodicTask: PeriodicTask = PeriodicTask({
        updateServiceStatusTime()
    }, 50000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initOnClickListeners()
        handleNotificationToken()
        makeOutOfService()
    }

    override fun onResume() {
        super.onResume()
        if (Lingver.getInstance().getLocale().toString() != super.currentLangCode) {
            recreate()
        } else {
            callGetDashboardInfo()
        }
    }

    private fun initData() {
        binding.textDriverName.text =
            "${loginResponse?.user?.firstName} ${loginResponse?.user?.lastName}"

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        if (companyConfigurations?.isDriverPickupPackagesByScanDisabled == true) {
            binding.dashEntryScanPackages.root.visibility = View.GONE
        }

        if (companyConfigurations?.isFulfilmentEnabled == true) {
            binding.containerWarehousePackages.visibility = View.VISIBLE
        }

        createLocationRequest()

        if (Helper.isLogesTechsDriver()) {
            binding.containerLogestechsLogoBottom.visibility = View.VISIBLE
        } else {
            binding.containerLogestechsLogoBottom.visibility = View.GONE
        }


        if (loginResponse?.user?.companyID == 240.toLong() || loginResponse?.user?.companyID == 313.toLong()) {
            binding.tvPackagesNumber.text = getString(R.string.dashboard_packages_number_sprint)
            binding.dashEntryScanPackages.titleText =
                getString(R.string.dashboard_scan_new_package_barcode_sprint)
            binding.dashEntryAcceptedPackages.titleText =
                getString(R.string.dashboard_accepted_packages_sprint)
            binding.dashEntryPendingPackages.titleText =
                getString(R.string.dashboard_pending_packages_sprint)
            binding.dashEntryInCarPackages.titleText =
                getString(R.string.dashboard_in_car_packages_sprint)
            binding.dashEntryFailedPackages.titleText = getString(R.string.dashboard_failed_sprint)
            binding.dashSubEntryMassCodReports.titleText =
                getString(R.string.dashboard_mass_cod_reports_sprint)
            binding.dashSubEntryDraftPickups.titleText =
                getString(R.string.title_draft_pickups_sprint)
            binding.dashSubEntryReturnedPackages.titleText =
                getString(R.string.title_returned_packages_sprint)
        } else {
            binding.dashEntryScanPackages.titleText =
                getString(R.string.dashboard_scan_new_package_barcode)
            binding.dashEntryAcceptedPackages.titleText =
                getString(R.string.dashboard_accepted_packages)
            binding.dashEntryPendingPackages.titleText =
                getString(R.string.dashboard_pending_packages)
            binding.dashEntryPendingPackages.titleText =
                getString(R.string.dashboard_pending_packages)
            binding.dashEntryInCarPackages.titleText = getString(R.string.dashboard_in_car_packages)
            binding.dashEntryFailedPackages.titleText = getString(R.string.dashboard_failed)
            binding.dashSubEntryMassCodReports.titleText =
                getString(R.string.dashboard_mass_cod_reports)
            binding.dashSubEntryDraftPickups.titleText = getString(R.string.title_draft_pickups)
            binding.dashSubEntryReturnedPackages.titleText =
                getString(R.string.title_returned_packages)
        }

    }

    private fun initOnClickListeners() {
        binding.buttonShowDashboardSubEntries.setOnClickListener(this)
        binding.imageViewDriverLogo.setOnClickListener(this)
        binding.dashEntryPendingPackages.root.setOnClickListener(this)
        binding.dashEntryAcceptedPackages.root.setOnClickListener(this)
        binding.dashEntryInCarPackages.root.setOnClickListener(this)
        binding.dashEntryScanPackages.root.setOnClickListener(this)
        binding.dashEntryFailedPackages.root.setOnClickListener(this)
        binding.dashEntryPostponedPackages.root.setOnClickListener(this)
        binding.dashSubEntryBroughtPackages.root.setOnClickListener(this)
        binding.dashSubEntryReturnedPackages.root.setOnClickListener(this)
        binding.dashSubEntryMassCodReports.root.setOnClickListener(this)
        binding.dashSubEntryDraftPickups.root.setOnClickListener(this)
        binding.dashSubEntryDriverRoute.root.setOnClickListener(this)
        binding.dashSubEntryCheckIns.root.setOnClickListener(this)
        binding.containerServiceTypeView.setOnClickListener(this)
        binding.dashEntryWarehousePackages.root.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun bindDashboardData(data: GetDashboardInfoResponse?) {
        binding.dashEntryAcceptedPackages.textCount.text = data?.acceptedPackagesCount.toString()
        binding.dashEntryInCarPackages.textCount.text = data?.inCarPackagesCount.toString()
        binding.dashEntryPendingPackages.textCount.text = data?.pendingPackagesCount.toString()
        binding.dashEntryPostponedPackages.textCount.text = data?.postponedPackagesCount.toString()
        binding.dashEntryFailedPackages.textCount.text = data?.failedPackagesCount.toString()
        binding.dashEntryScanPackages.textCount.visibility = View.GONE

        binding.textInCarPackagesCount.text = data?.inCarPackagesCount.toString()
        binding.textDeliveredPackagesCount.text = data?.deliveredPackagesCount.toString()

        binding.textMassCodReportsSum.text =
            "${Helper.getCompanyCurrency()} ${data?.carriedMassReportsSum?.format()}"
        binding.textCodSum.text = "${Helper.getCompanyCurrency()} ${data?.carriedCodSum?.format()}"


        if (data?.isDriverOnline == true) {
            stopServiceStatusTimer()
            serviceStatusReferenceDate = if (data.onlineStartTime != null) {
                Helper.getDateFromServer(data.onlineStartTime)
            } else {
                Date()
            }
            updateServiceStatusTime()
            startServiceStatusTimer()
            makeInService()
        } else {
            serviceStatusReferenceDate = null
            makeOutOfService()
        }
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

    private fun makeInService() {
        isInService = true
        binding.textInService.visibility = View.VISIBLE
        binding.textOutOfService.visibility = View.GONE
        binding.containerDuration.visibility = View.VISIBLE
        binding.imageServiceStatus.setImageResource(R.drawable.ic_in_service_clock)
        binding.containerServiceTypeView.setBackgroundResource(R.drawable.background_in_service_oval)
    }

    private fun makeOutOfService() {
        stopServiceStatusTimer()
        isInService = false
        binding.textInService.visibility = View.GONE
        binding.textOutOfService.visibility = View.VISIBLE
        binding.containerDuration.visibility = View.GONE
        binding.imageServiceStatus.setImageResource(R.drawable.ic_out_of_service_clock)
        binding.containerServiceTypeView.setBackgroundResource(R.drawable.background_out_of_service_oval)
    }

    private fun updateServiceStatusTime() {
        val diff = Date().time - (serviceStatusReferenceDate ?: Date()).time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        binding.textMinutes.text = (minutes % 60).toString()
        binding.textHours.text = hours.toString()
    }

    private fun startServiceStatusTimer() {
        periodicTask.startUpdates()
    }

    private fun stopServiceStatusTimer() {
        periodicTask.stopUpdates()
    }

    //:- Action Handlers
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_show_dashboard_sub_entries -> {
                if (binding.containerDashboardSubEntries.visibility == View.VISIBLE) {
                    binding.containerDashboardSubEntries.visibility = View.GONE
                } else {
                    binding.containerDashboardSubEntries.visibility = View.VISIBLE
                    binding.scrollView.postDelayed(Runnable {
                        binding.scrollView.fullScroll(
                            ScrollView.FOCUS_DOWN
                        )
                    }, 250)
                }
            }

            R.id.image_view_driver_logo -> {
                val mIntent = Intent(this, ProfileActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_pending_packages -> {
                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 0)
                startActivity(mIntent)
            }

            R.id.dash_entry_accepted_packages -> {
                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 1)
                startActivity(mIntent)
            }

            R.id.dash_entry_in_car_packages -> {
                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 2)
                startActivity(mIntent)
            }

            R.id.dash_entry_scan_packages -> {
                val mIntent = Intent(this, BarcodeScannerActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_entry_postponed_packages -> {
                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 2)
                mIntent.putExtra(
                    IntentExtrasKeys.IN_CAR_PACKAGE_STATUS.name,
                    InCarPackageStatus.POSTPONED.name
                )
                startActivity(mIntent)
            }

            R.id.dash_entry_failed_packages -> {
                val mIntent = Intent(this, DriverPackagesByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 2)
                mIntent.putExtra(
                    IntentExtrasKeys.IN_CAR_PACKAGE_STATUS.name,
                    InCarPackageStatus.FAILED.name
                )
                startActivity(mIntent)
            }

            R.id.dash_sub_entry_brought_packages -> {
                val mIntent = Intent(this, BroughtPackagesActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_sub_entry_returned_packages -> {
                val mIntent = Intent(this, ReturnedPackagesActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_sub_entry_mass_cod_reports -> {
                val mIntent = Intent(this, MassCodReportsActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_sub_entry_draft_pickups -> {
                val mIntent = Intent(this, DriverDraftPickupsByStatusViewPagerActivity::class.java)
                mIntent.putExtra(IntentExtrasKeys.SELECTED_PACKAGES_TAB.name, 0)
                startActivity(mIntent)
            }

            R.id.dash_sub_entry_driver_route -> {
                val mIntent = Intent(this, DriverRouteActivity::class.java)
                startActivity(mIntent)
            }

            R.id.dash_sub_entry_check_ins -> {
                val mIntent = Intent(this, CheckInActivity::class.java)
                startActivity(mIntent)
            }

            R.id.container_service_type_view -> {
                callChangeWorkLogStatus()
            }

            R.id.dash_entry_warehouse_packages -> {
                val mIntent = Intent(this, WarehousePackagesByStatusViewPagerActivity::class.java)
                startActivity(mIntent)
            }
        }
    }

    //Location handling
    var mLocationListeners: Array<LocationListener> =
        arrayOf(
            LocationListener(
                LocationManager.NETWORK_PROVIDER
            ),
            LocationListener(
                LocationManager.GPS_PROVIDER
            )
        )

    private fun createLocationRequest() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                AppConstants.REQUEST_LOCATION_PERMISSION
            )
            return
        }
        restartLocationService()
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                AppConstants.REQUEST_LOCATION_PERMISSION
            )
            return
        }
    }

    private fun restartLocationService() {
        cancelAlarm()
        startAlarm()
    }

    private fun cancelAlarm() {
        val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
        val myIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getBroadcast(
                    this,
                    0,
                    myIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        alarmManager.cancel(pendingIntent)
        val myService = Intent(this, MyLocationService::class.java)
        stopService(myService)

    }

    private fun startAlarm() {
        val alarm = AlarmReceiver()
        alarm.setAlarm(this)
        val intent = Intent(this, MyLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopGetLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
        }
        if (mLocationManager != null) {
            mLocationManager!!.removeUpdates(mLocationListeners[0])
        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                if (location != null) {
                    updateLocation(location)
                }
            }
        }
    }

    //:-APIs 
    private fun callGetDashboardInfo() {
        showWaitDialog()
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getDashboardInfo(loginResponse?.device?.id)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        val data = response.body()
                        withContext(Dispatchers.Main) {
                            bindDashboardData(data)
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

    private fun callChangeWorkLogStatus() {
        showWaitDialog()
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.changeWorkLogStatus(
                        ChangeWorkLogStatusRequestBody(
                            !isInService,
                            if (isInService) SharedPreferenceWrapper.getWorkLogId()?.id else null,
                            loginResponse?.device?.id
                        )
                    )
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            if (isInService) {
                                makeOutOfService()
                            } else {
                                makeInService()
                                serviceStatusReferenceDate = Date()
                                startServiceStatusTimer()
                                SharedPreferenceWrapper.saveWorkLogId(response.body())
                            }
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
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
                    hideWaitDialog()
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
            hideWaitDialog()
            Helper.showErrorMessage(
                super.getContext(), getString(R.string.error_check_internet_connection)
            )
        }
    }

    private fun updateLocation(location: Location) {
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.updateDriverLocation(
                        UpdateLocationRequestBody(
                            location.latitude,
                            location.longitude,
                            loginResponse?.user?.companyID,
                            loginResponse?.user?.id,
                            loginResponse?.user?.vehicle?.id,
                            Timestamp(location.time).toString()
                        )
                    )
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {

                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                }
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