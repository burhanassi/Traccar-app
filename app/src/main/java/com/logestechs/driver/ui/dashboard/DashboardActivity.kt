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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.UpdateLocationRequestBody
import com.logestechs.driver.api.responses.GetDashboardInfoResponse
import com.logestechs.driver.databinding.ActivityDashboardBinding
import com.logestechs.driver.ui.barcodeScanner.BarcodeScannerActivity
import com.logestechs.driver.ui.driverDraftPickupsByStatusViewPager.DriverDraftPickupsByStatusViewPagerActivity
import com.logestechs.driver.ui.driverPackagesByStatusViewPager.DriverPackagesByStatusViewPagerActivity
import com.logestechs.driver.ui.massCodReports.MassCodReportsActivity
import com.logestechs.driver.ui.profile.ProfileActivity
import com.logestechs.driver.ui.returnedPackages.ReturnedPackagesActivity
import com.logestechs.driver.utils.*
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

class DashboardActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDashboardBinding
    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initOnClickListeners()
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
        createLocationRequest()
    }

    private fun initOnClickListeners() {
        binding.buttonShowDashboardSubEntries.setOnClickListener(this)
        binding.imageViewDriverLogo.setOnClickListener(this)
        binding.dashEntryPendingPackages.root.setOnClickListener(this)
        binding.dashEntryAcceptedPackages.root.setOnClickListener(this)
        binding.dashEntryInCarPackages.root.setOnClickListener(this)
        binding.dashEntryScanPackages.root.setOnClickListener(this)
        binding.dashSubEntryReturnedPackages.root.setOnClickListener(this)
        binding.dashSubEntryMassCodReports.root.setOnClickListener(this)
        binding.dashSubEntryDraftPickups.root.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun bindDashboardData(data: GetDashboardInfoResponse?) {
        binding.dashEntryAcceptedPackages.textCount.text = data?.acceptedPackagesCount.toString()
        binding.dashEntryInCarPackages.textCount.text = data?.inCarPackagesCount.toString()
        binding.dashEntryPendingPackages.textCount.text = data?.pendingPackagesCount.toString()
        binding.dashEntryScanPackages.textCount.visibility = View.GONE

        binding.textInCarPackagesCount.text = data?.inCarPackagesCount.toString()
        binding.textDeliveredPackagesCount.text = data?.deliveredPackagesCount.toString()

        binding.textMassCodReportsSum.text =
            "${Helper.getCompanyCurrency()} ${data?.carriedMassReportsSum.toString()}"
        binding.textCodSum.text = "${Helper.getCompanyCurrency()} ${data?.carriedCodSum.toString()}"
    }


    //:- Action Handlers
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_show_dashboard_sub_entries -> {
                if (binding.containerDashboardSubEntries.visibility == View.VISIBLE) {
                    binding.containerDashboardSubEntries.visibility = View.GONE
                } else {
                    binding.containerDashboardSubEntries.visibility = View.VISIBLE
                    binding.scrollView.scrollTo(0, binding.scrollView.bottom);
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
            PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT)
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
                    val response = ApiAdapter.apiClient.getDashboardInfo()
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
}