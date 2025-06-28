package com.logestechs.traccarApp.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.logestechs.traccarApp.BuildConfig
import com.logestechs.traccarApp.R
import com.logestechs.traccarApp.api.ApiAdapter
import com.logestechs.traccarApp.api.requests.LogExceptionRequestBody
import com.logestechs.traccarApp.api.requests.UpdateLocationRequestBody
import com.logestechs.traccarApp.data.model.Device
import com.logestechs.traccarApp.data.model.LatLng
import com.logestechs.traccarApp.databinding.ActivityDriverDashboardBinding
import com.logestechs.traccarApp.utils.*
import com.logestechs.traccarApp.utils.location.AlarmReceiver
import com.logestechs.traccarApp.utils.location.LocationListener
import com.logestechs.traccarApp.utils.location.MyLocationService
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.sql.Timestamp
import java.util.*

class DriverDashboardActivity : LogesTechsActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDriverDashboardBinding
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationManager: LocationManager? = null
    private var isServiceRunning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        initOnClickListeners()
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val lat = intent?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lng = intent?.getDoubleExtra("lng", 0.0) ?: 0.0

            findViewById<TextView>(R.id.text_location)?.text = "Lat: $lat, Lng: $lng"
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("LOCATION_UPDATE")
        registerReceiver(locationReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationReceiver)
    }

    private fun initData() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        createLocationRequest()
    }

    private fun initOnClickListeners() {
        binding.circleButton.setOnClickListener(this)
    }

    //:- Action Handlers
    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.circleButton -> {
                if (!isServiceRunning) {
                    startForegroundService(Intent(this, MyLocationService::class.java))
                    binding.circleButton.text = "Stop"
                    binding.circleButton.background = getDrawable(R.drawable.circle_button_background_stop)
                } else {
                    stopService(Intent(this, MyLocationService::class.java))
                    binding.circleButton.text = "Go"
                    binding.circleButton.background = getDrawable(R.drawable.circle_button_background)
                }
                isServiceRunning = !isServiceRunning
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
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                AppConstants.REQUEST_LOCATION_PERMISSION
            )
            return
        }

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



    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = locationResult.lastLocation
            if (currentLocation != null) {
                val previousLocation = SharedPreferenceWrapper.getLastSyncLocation()?.let { latLng ->
                    Location("").apply {
                        latitude = latLng.lat ?: 0.0
                        longitude = latLng.lng ?: 0.0
                    }
                }
                if (previousLocation != null) {
                    val distance = previousLocation.distanceTo(currentLocation)
                    if (distance > 1000) {
                        updateLocation(currentLocation)
                    }
                } else {
                    updateLocation(currentLocation)
                }
            }
        }
    }

    //:-APIs
    private fun updateLocation(location: Location) {
        if (Helper.isInternetAvailable(super.getContext())) {
            GlobalScope.launch(Dispatchers.IO) {
                val deviceId = SharedPreferenceWrapper.getUUID() // Unique device ID
                val url = "http://192.168.1.127:5055/?id=$deviceId" +
                        "&lat=${location.latitude}" +
                        "&lon=${location.longitude}" +
                        "&timestamp=${System.currentTimeMillis()}" +
                        "&speed=${location.speed * 3.6}"
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()
                    client.newCall(request).execute()

                    withContext(Dispatchers.Main) {
                        SharedPreferenceWrapper.saveLastSyncLocation(
                            LatLng(location.latitude, location.longitude)
                        )

                        val intent = Intent("LOCATION_UPDATE")
                        intent.putExtra("lat", location.latitude)
                        intent.putExtra("lng", location.longitude)
                        sendBroadcast(intent)
                    }
                } catch (e: Exception) {
                    Helper.logException(e, "Location send failed")
                }
            }
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, MyLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            AppConstants.REQUEST_LOCATION_PERMISSION
        )
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