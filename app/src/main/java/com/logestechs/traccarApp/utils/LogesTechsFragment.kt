package com.logestechs.traccarApp.utils

import android.content.Context
import androidx.fragment.app.Fragment

open class LogesTechsFragment : Fragment() {

    open fun showWaitDialog() {
        try {
            if ((requireActivity() is LogesTechsActivity)) {
                (requireActivity() as LogesTechsActivity).showWaitDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun hideWaitDialog() {
        try {
            if ((requireActivity() is LogesTechsActivity)) {
                (requireActivity() as LogesTechsActivity).hideWaitDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

//    open fun getLocationPermission(): Boolean? {
//        if (context == null) {
//            return false
//        }
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            )
//            == PackageManager.PERMISSION_GRANTED
//        ) return true else requestPermissions(
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//            AppConstants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
//        )
//        return false
//    }
//
//    open fun getCameraPermission(): Boolean? {
//        if (context == null) {
//            return false
//        }
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.CAMERA
//            )
//            == PackageManager.PERMISSION_GRANTED
//        ) {
//            return true
//        } else {
//            requestPermissions(
//                arrayOf(Manifest.permission.CAMERA),
//                AppConstants.PERMISSIONS_REQUEST_PHONE_CAMERA
//            )
//        }
//        return false
//    }

    override fun getContext(): Context? {
        return activity
    }
}