package com.logestechs.traccarApp.utils.location

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
import com.logestechs.traccarApp.BuildConfig
import com.logestechs.traccarApp.api.ApiAdapter
import com.logestechs.traccarApp.api.requests.UpdateLocationRequestBody
import com.logestechs.traccarApp.data.model.LatLng
import com.logestechs.traccarApp.utils.Helper
import com.logestechs.traccarApp.utils.LogesTechsApp
import com.logestechs.traccarApp.utils.PermissionRequestActivity
import com.logestechs.traccarApp.utils.SharedPreferenceWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp
import com.logestechs.traccarApp.R
import okhttp3.OkHttpClient
import okhttp3.Request


class MyLocationService : Service() {
    //region data
    private val UPDATE_INTERVAL_IN_MILLISECONDS = (5 * 60 * 1000).toLong()
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var manager: NotificationManager? = null
    private var NOTIFICATION_ID = (System.currentTimeMillis() % 10000).toInt()
    var factor = 1000.0

    private val MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000

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

    override fun onDestroy() {
        super.onDestroy()

        mFusedLocationClient?.removeLocationUpdates(locationCallback)
        stopForeground(true)

        GlobalScope.launch(Dispatchers.IO) {
            val deviceId = SharedPreferenceWrapper.getUUID()
            val url = "http://192.168.1.127:8082/?id=$deviceId" +
                    "&lat=0.0&lon=0.0" +
                    "&timestamp=${System.currentTimeMillis()}" +
                    "&status=stopped"

            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute()
            } catch (e: Exception) {
                Helper.logException(e, "Failed to notify Traccar on stop")
            }
        }
    }

    private fun initData() {
        locationRequest = LocationRequest.create()
        locationRequest?.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(LogesTechsApp.instance)
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
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
                val previousLocation = SharedPreferenceWrapper.getLastSyncLocation()?.let { latLng ->
                    Location("").apply {
                        latitude = latLng.lat ?: 0.0
                        longitude = latLng.lng ?: 0.0
                    }
                }
                if (previousLocation != null) {
                    val distance = previousLocation.distanceTo(currentLocation)
                    if (distance > MIN_DISTANCE_CHANGE_FOR_UPDATES) {
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

        if (checkPermissions()) {
            initData()
            startLocationUpdates()
        } else {
            requestPermissions()
        }

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

        mFusedLocationClient!!.lastLocation.addOnSuccessListener { location ->
            location?.let {
                updateLocation(it)
            }
        }
    }

    private fun updateLocation(location: Location) {
        GlobalScope.launch(Dispatchers.IO) {
            val deviceId = SharedPreferenceWrapper.getUUID() // Unique device ID
            val url = "http://192.168.1.127:8082/?id=$deviceId" +
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

    fun stopService() {
        if (manager != null) {
            manager!!.cancel(NOTIFICATION_ID)
        }
    }
}