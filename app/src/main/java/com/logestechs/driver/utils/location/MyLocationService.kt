package com.logestechs.driver.utils.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.UpdateLocationRequestBody
import com.logestechs.driver.data.model.LatLng
import com.logestechs.driver.data.model.User
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.PermissionRequestActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import kotlin.math.floor


class MyLocationService : Service() {
    //region data
    private val UPDATE_INTERVAL_IN_MILLISECONDS = (60 * 1000).toLong()
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private val locationSettingsRequest: LocationSettingsRequest? = null
    private var manager: NotificationManager? = null
    private var NOTIFICATION_ID = (System.currentTimeMillis() % 10000).toInt()
    private var myProfile: User? = null
    var factor = 1000.0

    private var loginResponse = SharedPreferenceWrapper.getLoginResponse()
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (checkPermissions()) {
            initData()
        } else {
            requestPermissions()
        }
    }

    private fun initData() {
        locationRequest = LocationRequest.create()
        locationRequest?.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        myProfile = SharedPreferenceWrapper.getLoginResponse()?.user
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(LogesTechsApp.instance)
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val intent = Intent(this, PermissionRequestActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    //Location Callback
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = locationResult.lastLocation
            if (currentLocation != null) {
                val previousLocation = SharedPreferenceWrapper.getLastSyncLocation()
                if (previousLocation != null) {
                    if (!(floor((previousLocation.lat ?: 0.0) * factor) / factor ==
                                floor(currentLocation.latitude * factor) / factor &&
                                floor((previousLocation.lng ?: 0.0) * factor) / factor ==
                                floor(currentLocation.longitude * factor) / factor)
                    ) {
                        updateLocation(currentLocation)
                    }
                } else {
                    updateLocation(currentLocation)
                }
            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareForegroundNotification()
        }
        startLocationUpdates()
        return START_STICKY
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun prepareForegroundNotification() {
        val NOTIFICATION_CHANNEL_ID: String = BuildConfig.APPLICATION_ID
        val channelName = "Location Service Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_NONE
            )
            manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager?.createNotificationChannel(serviceChannel)

        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_customer_logo)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setContentTitle("Get Location is running in background")
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient!!.requestLocationUpdates(
            locationRequest!!,
            locationCallback,
            Looper.myLooper()
        )
    }

    private fun updateLocation(location: Location) {
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
                        SharedPreferenceWrapper.saveLastSyncLocation(
                            LatLng(
                                location.latitude,
                                location.longitude
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Helper.logException(e, Throwable().stackTraceToString())
            }
        }
    }

    fun stopService() {
        if (manager != null) {
            manager!!.cancel(NOTIFICATION_ID)
        }
    }
}