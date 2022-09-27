package com.logestechs.driver.utils.location

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class LocationListener(provider: String) : LocationListener {
    var mLastLocation: Location

    init {
        mLastLocation = Location(provider)
    }

    override fun onLocationChanged(location: Location) {
        mLastLocation.set(location)
    }

    override fun onProviderDisabled(provider: String) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
    }
}
