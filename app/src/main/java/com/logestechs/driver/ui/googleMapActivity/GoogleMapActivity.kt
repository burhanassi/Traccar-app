package com.logestechs.driver.ui.googleMapActivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.logestechs.driver.R
import com.logestechs.driver.api.responses.GetDriverPackagesLocationsResponse
import com.logestechs.driver.data.model.ServerLatLng
import com.logestechs.driver.databinding.ActivityGoogleMapBinding
import com.logestechs.driver.utils.AppConstants.Companion.REQUEST_LOCATION_PERMISSION
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.IntentExtrasKeys
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.LocationPackageItemAdapter


class GoogleMapActivity : LogesTechsActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    private lateinit var binding: ActivityGoogleMapBinding
    private var mMap: GoogleMap? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var userLocation: LatLng? =
        SharedPreferenceWrapper.getLastSyncLocation()?.toGoogleLatLng()
    private var extraMarkerInfo: HashMap<String, List<ServerLatLng?>> = HashMap()
    private var getDriverPackagesLocationsResponse: GetDriverPackagesLocationsResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExtras()
        binding.numberOfLocationsLabel.text = getString(
            R.string.text_number_of_locations,
            getDriverPackagesLocationsResponse?.items?.size
        )
        val mapFragment: SupportMapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getMyLocation()
    }

    override fun onResume() {
        super.onResume()
        if (fusedLocationProviderClient == null) fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(
                this
            )
        checkPermissions()
    }


    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.setOnMarkerClickListener(this)
        if (checkPermission()) mMap?.isMyLocationEnabled = true else askPermission()
        mMap?.isMyLocationEnabled = true
        if (getDriverPackagesLocationsResponse?.geometry != null && getDriverPackagesLocationsResponse?.geometry?.isNotEmpty() == true
        ) {
            val locations: List<LatLng> =
                Helper.decodeMapGeometry(getDriverPackagesLocationsResponse?.geometry ?: "")
            mMap?.addPolyline(
                PolylineOptions().addAll(locations)
                    .width // below line is use to specify the width of poly line.
                        (8f) // below line is use to add color to our poly line.
                    .color(Color.BLUE) // below line is to make our poly line geodesic.
                    .geodesic(true)
            )

            mMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        userLocation?.latitude ?: 0.0,
                        userLocation?.longitude ?: 0.0
                    ), 10f
                )
            )
        }

        if (getDriverPackagesLocationsResponse?.items?.isNotEmpty() == true) {
            var index = 0
            for (item in getDriverPackagesLocationsResponse?.items!!) {
                index++
                val position = LatLng(
                    item[0]?.latitude ?: 0.0,
                    item[0]?.longitude ?: 0.0
                )
                var marker: Marker?

                if (item.size > 1) {
                    val parts = item[0]?.label?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }
                        ?.toTypedArray()
                    val locationPart = parts?.get(0)
                    marker = mMap!!.addMarker(
                        MarkerOptions()
                            .position(position)
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    Helper.writeTextOnDrawable(
                                        this,
                                        resources,
                                        R.drawable.ic_location_red,
                                        index.toString() + ""
                                    )
                                )
                            )
                            .title(locationPart)
                    )!!
                } else {
                    marker = mMap!!.addMarker(
                        MarkerOptions()
                            .position(position)
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    Helper.writeTextOnDrawable(
                                        this,
                                        resources,
                                        R.drawable.ic_location_green,
                                        index.toString() + ""
                                    )
                                )
                            )
                            .title(item[0]?.label)
                    )
                }
                if (marker != null) {
                    extraMarkerInfo[marker.id] = item
                }
            }
            if (userLocation != null) {
                mMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            userLocation?.latitude ?: 0.0,
                            userLocation?.longitude ?: 0.0
                        ), 10f
                    )
                )

            }
        }
    }


    // Check for permission to access Location
    private fun checkPermission(): Boolean {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
    }

    // Asks for permission
    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission granted
                    if (checkPermission()) mMap?.isMyLocationEnabled = true
                } else {
                    // Permission denied
                }
            }

            else -> throw IllegalStateException("Unexpected value: $requestCode")
        }
    }

    private fun checkPermissions() {
        userLocation = SharedPreferenceWrapper.getLastSyncLocation()?.toGoogleLatLng()
        if (userLocation != null) return
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList(1)
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (listPermissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    listPermissionsNeeded.toTypedArray(),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        } else {
            getMyLocation()
        }
    }


    private fun getMyLocation() {
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            if (fusedLocationProviderClient == null) fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = com.logestechs.driver.data.model.LatLng(
                        location.latitude,
                        location.longitude
                    )
                    SharedPreferenceWrapper.saveLastSyncLocation(latLng)
                }
            }
        }
    }


    private fun getExtras() {
        val extras = intent.extras
        if (extras != null) {
            getDriverPackagesLocationsResponse =
                extras.getParcelable(IntentExtrasKeys.DRIVER_PACKAGES_LOCATIONS.name)
        }
    }

    //dialogs
    private fun showLocationPackagesDialog(packagesLocations: List<ServerLatLng?>) {
        val dialogBuilder = AlertDialog.Builder(this, 0)
        val inflater = LayoutInflater.from(this)
        val dialogView: View = inflater.inflate(R.layout.dialog_location_packages, null)
        dialogBuilder.setView(dialogView)
        val alertDialog = dialogBuilder.create()
        val mDoneButton = dialogView.findViewById<Button>(R.id.button_done)
        val packagesRecyclerView = dialogView.findViewById<RecyclerView>(R.id.rv_packages)
        val adapter = LocationPackageItemAdapter(packagesLocations)
        packagesRecyclerView.layoutManager = LinearLayoutManager(this)
        packagesRecyclerView.adapter = adapter
        adapter.update(packagesLocations)
        mDoneButton.setOnClickListener { alertDialog.dismiss() }
        alertDialog.setCanceledOnTouchOutside(true)
        if (alertDialog.window != null) alertDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        alertDialog.show()
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        val markerInfo: List<ServerLatLng?>? = extraMarkerInfo[marker.id]
        if (markerInfo != null) {
            if (markerInfo.size > 1) {
                showLocationPackagesDialog(markerInfo)
            } else {
                marker.showInfoWindow()
            }
        }
        return false
    }
}