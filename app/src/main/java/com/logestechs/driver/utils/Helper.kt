package com.logestechs.driver.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.logestechs.driver.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


class Helper {
    companion object {
        var toast: Toast? = null
        fun isInternetAvailable(context: Context?): Boolean {
            var result = false
            if (context != null) {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val networkCapabilities = connectivityManager.activeNetwork ?: return false
                    val actNw =
                        connectivityManager.getNetworkCapabilities(networkCapabilities)
                            ?: return false
                    result = when {
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                } else {
                    connectivityManager.run {
                        connectivityManager.activeNetworkInfo?.run {
                            result = when (type) {
                                ConnectivityManager.TYPE_WIFI -> true
                                ConnectivityManager.TYPE_MOBILE -> true
                                ConnectivityManager.TYPE_ETHERNET -> true
                                else -> false
                            }
                        }
                    }
                }
            }
            return result
        }

        @SuppressLint("InflateParams")
        fun showErrorMessage(context: Context?, message: String?) {
            if (context != null) {
                toast?.cancel()
                val layoutInflater = LayoutInflater.from(context)
                val layout: View = layoutInflater.inflate(R.layout.custom_toast_fail, null)
                val text = layout.findViewById<TextView>(R.id.text)
                text.text = message
                toast = Toast(context)
                toast?.setGravity(Gravity.TOP, 0, 70)
                toast?.duration = Toast.LENGTH_LONG
                toast?.view = layout
                toast?.show()
            }
        }

        @SuppressLint("InflateParams")
        fun showSuccessMessage(context: Context?, message: String?) {
            if (context != null) {
                toast?.cancel()
                val layoutInflater = LayoutInflater.from(context)
                val layout: View = layoutInflater.inflate(R.layout.custom_toast_success, null)
                val text = layout.findViewById<TextView>(R.id.text)
                text.text = message
                toast = Toast(context)
                toast?.setGravity(Gravity.TOP, 0, 70)
                toast?.duration = Toast.LENGTH_LONG
                toast?.view = layout
                toast?.show()
            }
        }

        fun logException(exception: Exception, stackTrace: String) {
//            FirebaseCrashlytics.getInstance().log(stackTrace)
//            FirebaseCrashlytics.getInstance()
//                .recordException(exception)
        }

        fun changeStatusBarColor(activity: Activity, colorID: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = colorID
            }
        }

        fun changeImageStrokeColor(imageView: ImageView?, color: Int, context: Context?) {
            DrawableCompat.setTint(
                DrawableCompat.wrap(imageView?.drawable!!),
                ContextCompat.getColor(context!!, color)
            )
        }

        fun validatePassword(password: String): Boolean {
            if (password.length >= 6)
                return true
            return false
        }

        fun restartApplication(context: Context) {
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName = intent?.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }

        fun getCompanyCurrency(): String {
            val currency = SharedPreferenceWrapper.getLoginResponse()?.user?.currency
            return if (currency == "NIS") {
                AppCurrency.NIS.value
            } else {
                currency ?: ""
            }
        }

        fun replaceArabicNumbers(original: String): String? {
            return original
                .replace("٠".toRegex(), "0")
                .replace("١".toRegex(), "1")
                .replace("٢".toRegex(), "2")
                .replace("٣".toRegex(), "3")
                .replace("٤".toRegex(), "4")
                .replace("٥".toRegex(), "5")
                .replace("٦".toRegex(), "6")
                .replace("٧".toRegex(), "7")
                .replace("٨".toRegex(), "8")
                .replace("٩".toRegex(), "9")
        }

        fun formatNumberForWhatsApp(mobileNumber: String?, isSecondary: Boolean = false): String {
            var number = mobileNumber ?: ""
            if (isSecondary) {
                return if (number.length == 10) {
                    number.drop(1)
                    number = "+972$number"
                    number
                } else if (number.length == 9 && number[0] == '5') {
                    number = "+972$number"
                    number
                } else if (number.length == 12) {
                    number = "+$number"
                    number
                } else {
                    number
                }
            } else {
                when (getCompanyCurrency()) {
                    AppCurrency.NIS.value -> {
                        return if (number.length == 10) {
                            number.drop(1)
                            number = "+970$number"
                            number
                        } else if (number.length == 9 && number[0] == '5') {
                            number = "+970$number"
                            number
                        } else if (number.length == 12) {
                            number = "+$number"
                            number
                        } else {
                            number
                        }
                    }
                }
            }
            return number
        }

        fun getGoogleNavigationUrl(userLat: Double?, userLng: Double?): String? {
            return "http://maps.google.com/maps?daddr=" +
                    userLat + ", " + userLng
        }

        fun Double.format(): String {
            return if (this % 1.0 != 0.0) {
                val decimalSymbol = DecimalFormatSymbols(Locale.US)
                val df = DecimalFormat("##.###")
                df.decimalFormatSymbols = decimalSymbol
                df.format(this).toString()
            } else {
                (this.toInt().toString())
            }
        }

        fun getLocalizedInCarStatus(
            context: Context?,
            inCarPackageStatus: InCarPackageStatus
        ): String {
            when (inCarPackageStatus) {
                InCarPackageStatus.TO_DELIVER -> {
                    return context?.getString(R.string.in_car_status_to_deliver) ?: ""
                }
                InCarPackageStatus.ALL -> {
                    return context?.getString(R.string.in_car_status_all) ?: ""
                }
                InCarPackageStatus.POSTPONED -> {
                    return context?.getString(R.string.in_car_status_postponed) ?: ""
                }
                InCarPackageStatus.COD -> {
                    return context?.getString(R.string.in_car_status_cod) ?: ""
                }
                InCarPackageStatus.FAILED -> {
                    return context?.getString(R.string.in_car_status_failed) ?: ""
                }
                else -> {
                    return ""
                }
            }
        }
    }
}