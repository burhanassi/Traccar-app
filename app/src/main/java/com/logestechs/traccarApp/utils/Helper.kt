package com.logestechs.traccarApp.utils

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.logestechs.traccarApp.BuildConfig
import com.logestechs.traccarApp.R
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper

enum class AppFonts(val value: Int) {
    ROBOTO_BOLD(R.font.roboto_bold),
    ROBOTO_MEDIUM(R.font.roboto_medium),
    ROBOTO_LIGHT(R.font.roboto_light),
}

class Helper {
    companion object {
        var toast: Toast? = null

        fun getFontStyle(context: Context, font: AppFonts): Typeface? {
            return ResourcesCompat.getFont(context, font.value)
        }

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

        fun formatServerDate(source: String?, format: DateFormats): String {
            if (source != null) {
                val parser = SimpleDateFormat(DateFormats.SERVER_FORMAT.value, Locale.US)
                val formatter = SimpleDateFormat(format.value, Locale.US)
                return formatter.format(parser.parse(source)!!)
            }
            return ""
        }

        @SuppressLint("InflateParams")
        fun showErrorMessage(context: Context?, message: String?) {
            if (context != null) {
                toast?.cancel()
                val layoutInflater = LayoutInflater.from(context)
                val layout: View = layoutInflater.inflate(R.layout.custom_toast_fail, null)
                val text = layout.findViewById<TextView>(R.id.text)
                text.text = message

                Handler(Looper.getMainLooper()).post {
                    toast = Toast(context)
                    toast?.setGravity(Gravity.TOP, 0, 70)
                    toast?.duration = Toast.LENGTH_LONG
                    toast?.view = layout
                    toast?.show()
                }
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
            FirebaseCrashlytics.getInstance().log(stackTrace)
            FirebaseCrashlytics.getInstance()
                .recordException(exception)
        }

        fun getDeviceInfo(): String {
            val info = StringBuilder()

            info.append("MODEL ${Build.MODEL}")
            info.append("\n")

            info.append("MANUFACTURER ${Build.MANUFACTURER}")
            info.append("\n")

            info.append("Version Code ${Build.VERSION.RELEASE}")
            info.append("\n")

            info.append("SDK Level ${Build.VERSION.SDK_INT}")
            info.append("\n")

            return info.toString()
        }

        fun validatePassword(password: String): Boolean {
            if (password.length >= 6)
                return true
            return false
        }

        fun isLogesTechsDriver(): Boolean {
            return BuildConfig.company_id.toLong() == 0L
        }

        fun isBackendDriver(): Boolean {
            return BuildConfig.company_id.toLong() == -1L
        }

        object PrinterConst {
            const val PRINTER_BLUETOOTH_ADDRESS = "34:81:F4:D3:AF:DB"
            const val PDF_EXTENSION = "applcation/pdf"
        }
    }
}