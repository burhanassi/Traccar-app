package com.logestechs.driver.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Camera
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.vision.CameraSource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.MobileNumberValidationResult
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.User
import com.yariksoffice.lingver.Lingver
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
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

        fun formatServerDate(source: String?, format: DateFormats): String {
            if (source != null) {
                val parser = SimpleDateFormat(DateFormats.SERVER_FORMAT.value, Locale.US)
                val formatter = SimpleDateFormat(format.value, Locale.US)
                return formatter.format(parser.parse(source)!!)
            }
            return ""
        }

        fun getDateFromServer(serverDate: String?): Date? {
            val df = SimpleDateFormat(DateFormats.SERVER_FORMAT.value, Locale.US)
            return try {
                df.parse(serverDate)
            } catch (e: Exception) {
                null
            }
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

        fun isLogesTechsDriver(): Boolean {
            return BuildConfig.company_id.toLong() == 0L
        }

        fun isBackendDriver(): Boolean {
            return BuildConfig.company_id.toLong() == -1L
        }

        fun getCompanyId(): Long {
            return BuildConfig.company_id.toLong()
        }

        fun getCameraFromCameraSource(cameraSource: CameraSource?): Camera? {
            val declaredFields = CameraSource::class.java.declaredFields

            for (field in declaredFields) {
                if (field.type === Camera::class.java) {
                    field.isAccessible = true

                    try {
                        return field.get(cameraSource) as Camera
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }

                    break
                }
            }
            return null
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
                    number = number.drop(1)
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
                            number = number.drop(1)
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
                    AppCurrency.JOD.value -> {
                        return if (number.length == 9) {
                            number = "+962$number"
                            number
                        } else if (number.length == 10) {
                            number = number.drop(1)
                            number = "+962$number"
                            number
                        } else if (number.length == 12) {
                            number = "+$number"
                            number
                        } else if (number.length == 13) {
                            number = number.drop(1)
                            number = "+$number"
                            number
                        } else if (number.length == 14) {
                            number = number.drop(2)
                            number = "+$number"
                            number
                        } else {
                            number
                        }
                    }
                    AppCurrency.SAR.value -> {
                        return if (number.length == 9) {
                            number = "+966$number"
                            number
                        } else if (number.length == 10) {
                            number = number.drop(1)
                            number = "+966$number"
                            number
                        } else if (number.length == 12) {
                            number = "+$number"
                            number
                        } else if (number.length == 13) {
                            number = number.drop(1)
                            number = "+$number"
                            number
                        } else if (number.length == 14) {
                            number = number.drop(2)
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

        fun validateMobileNumber(
            type: PhoneType,
            number: String?,
            paramCurrency: String? = null
        ): MobileNumberValidationResult {
            val currency = paramCurrency ?: getCompanyCurrency()
            if (number != null) {
                when (currency) {
                    AppCurrency.NIS.value -> {
                        when (type) {
                            PhoneType.MOBILE -> {
                                return if (number.count() == 9 || number.count() == 10) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("9 - 10", false)
                                }
                            }
                            PhoneType.TELEPHONE -> {
                                return if (number.count() == 9 || number.count() == 10) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("10", false)
                                }
                            }
                        }
                    }

                    AppCurrency.JOD.value -> {
                        when (type) {
                            PhoneType.MOBILE -> {
                                return if (number.count() == 9 || number.count() == 10) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("9 - 10", false)
                                }
                            }
                            PhoneType.TELEPHONE -> {
                                return if (number.count() == 9 || number.count() == 10) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("9 - 10", false)
                                }
                            }
                        }
                    }

                    AppCurrency.BHD.value -> {
                        when (type) {
                            PhoneType.MOBILE -> {
                                return if (number.count() > 6 || number.count() < 16) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("6 - 16", false)
                                }
                            }
                            PhoneType.TELEPHONE -> {
                                return if (number.count() > 6 || number.count() < 16) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("6 - 16", false)
                                }
                            }
                        }
                    }

                    AppCurrency.KWD.value -> {
                        when (type) {
                            PhoneType.MOBILE -> {
                                return if (number.count() > 6 || number.count() < 16) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("6 - 16", false)
                                }
                            }
                            PhoneType.TELEPHONE -> {
                                return if (number.count() > 6 || number.count() < 16) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("6 - 16", false)
                                }
                            }
                        }
                    }

                    AppCurrency.IQD.value -> {
                        when (type) {
                            PhoneType.MOBILE -> {
                                return if (number.count() == 11) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("11", false)
                                }
                            }
                            PhoneType.TELEPHONE -> {
                                return if (number.count() == 11) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("11", false)
                                }
                            }
                        }
                    }

                    AppCurrency.SAR.value -> {
                        when (type) {
                            PhoneType.MOBILE -> {
                                return if (number.count() == 10) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("10", false)
                                }
                            }
                            PhoneType.TELEPHONE -> {
                                return if (number.count() == 10) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("10", false)
                                }
                            }
                        }
                    }

                    AppCurrency.OMR.value -> {
                        when (type) {
                            PhoneType.MOBILE -> {
                                return if (number.count() == 8) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("8", false)
                                }
                            }
                            PhoneType.TELEPHONE -> {
                                return if (number.count() == 8) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("8", false)
                                }
                            }
                        }
                    }

                    AppCurrency.LYD.value -> {
                        when (type) {
                            PhoneType.MOBILE -> {
                                return if (number.count() == 9 || number.count() == 10) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("9 - 10", false)
                                }
                            }
                            PhoneType.TELEPHONE -> {
                                return if (number.count() == 9 || number.count() == 10) {
                                    MobileNumberValidationResult("", true)
                                } else {
                                    MobileNumberValidationResult("10", false)
                                }
                            }
                        }
                    }
                }
            } else {
                return MobileNumberValidationResult("9 - 10", false)
            }
            return MobileNumberValidationResult("9 - 10", false)
        }


        fun getGoogleNavigationUrl(userLat: Double?, userLng: Double?): String {
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
                    return context?.getString(R.string.in_car_status_pickup_and_delivery) ?: ""
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
                InCarPackageStatus.TO_DELIVER_PICKUP -> {
                    return context?.getString(R.string.in_car_status_pickup) ?: ""
                }
                InCarPackageStatus.TO_DELIVER_DELIVERY -> {
                    return context?.getString(R.string.in_car_status_delivery) ?: ""
                }
                InCarPackageStatus.RETURNED -> {
                    return context?.getString(R.string.in_car_status_returned) ?: ""
                }
                else -> {
                    return ""
                }
            }
        }

        fun copyTextToClipboard(context: Context?, text: String?) {
            val clipboardManager =
                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(text, text)
            clipboardManager.setPrimaryClip(clipData)

            showSuccessMessage(context, context.getString(R.string.success_text_copy))
        }

        fun getPackageTypes(): LinkedHashMap<String, String> {
            var map = LinkedHashMap<String, String>()
            map[PackageType.COD.name] =
                LogesTechsApp.instance.resources.getString(R.string.package_type_cod)
            map[PackageType.REGULAR.name] =
                LogesTechsApp.instance.resources.getString(R.string.package_type_regular)
            map[PackageType.SWAP.name] =
                LogesTechsApp.instance.resources.getString(R.string.package_type_swap)
            map[PackageType.BRING.name] =
                LogesTechsApp.instance.resources.getString(R.string.package_type_bring)
            return map
        }

        fun getInterpretedMessageFromTemplate(
            data: Any?,
            isToMultiple: Boolean,
            messageTemplate: String?,
            partnerName: String? = null
        ): String? {
            val loginResponse = SharedPreferenceWrapper.getLoginResponse()
            val loggedInUser: User? = loginResponse?.user
            var recipientName: String? = ""
            var barcode: String? = ""
            var driverName: String? = ""
            var driverPhone: String? = ""
            val hubName: String = ""
            var company: String? = ""
            var businessSenderName: String? = ""
            val companyDomain: String? =
                SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations?.companyDomain
            var cod: String? = ""
            var expectedDeliveryDate = ""
            var postponeDate: String? = ""

            var template: String? = ""

            if (messageTemplate != null) {
                template = messageTemplate
            }

            if (data is Package) {
                val pkg = data
                recipientName = pkg.receiverFirstName
                barcode = pkg.barcode
                businessSenderName = pkg.originalBusinessSenderName
                cod = pkg.cod?.format()
                if (pkg.expectedDeliveryDate != null) {
                    expectedDeliveryDate = formatServerDate(
                        pkg.expectedDeliveryDate.toString(),
                        DateFormats.MESSAGE_TEMPLATE_WITH_TIME
                    )
                }
                if (pkg.postponedDeliveryDate != null) {
                    postponeDate = formatServerDate(
                        pkg.postponedDeliveryDate.toString(),
                        DateFormats.DEFAULT_FORMAT
                    )
                }
            }
            driverName = loggedInUser?.firstName
            driverPhone = loggedInUser?.phone
            if (partnerName != null) {
                company = partnerName
            } else {
                company = loginResponse?.businessName
            }
            if (!isToMultiple) {
                if (recipientName != null && recipientName.isNotEmpty()) {
                    template = template?.replace(
                        SmsTemplateTag.NAME.tag.toRegex(),
                        recipientName
                    )
                } else {
                    template =
                        template?.replace(" " + SmsTemplateTag.NAME.tag.toRegex(), "")
                    template =
                        template?.replace(SmsTemplateTag.NAME.tag.toRegex(), "")
                }
            } else {
                template =
                    template?.replace(" " + SmsTemplateTag.NAME.tag.toRegex(), "")
                template = template?.replace(SmsTemplateTag.NAME.tag.toRegex(), "")
            }
            if (!isToMultiple) {
                if (barcode != null && !barcode.isEmpty()) {
                    template = template?.replace(
                        SmsTemplateTag.barcode.tag.toRegex(),
                        barcode
                    )
                } else {
                    template = template?.replace(
                        " " + SmsTemplateTag.barcode.tag.toRegex(),
                        ""
                    )
                    template =
                        template?.replace(SmsTemplateTag.barcode.tag.toRegex(), "")
                }
            } else {
                template =
                    template?.replace(" " + SmsTemplateTag.barcode.tag.toRegex(), "")
                template = template?.replace(SmsTemplateTag.barcode.tag.toRegex(), "")
            }
            if (!isToMultiple) {
                if (businessSenderName != null && !businessSenderName.isEmpty()) {
                    template = template?.replace(
                        SmsTemplateTag.storeName.tag.toRegex(),
                        businessSenderName
                    )
                } else {
                    template = template?.replace(
                        " " + SmsTemplateTag.storeName.tag.toRegex(),
                        ""
                    )
                    template =
                        template?.replace(SmsTemplateTag.storeName.tag.toRegex(), "")
                }
            } else {
                template = template?.replace(
                    " " + SmsTemplateTag.storeName.tag.toRegex(),
                    ""
                )
                template =
                    template?.replace(SmsTemplateTag.storeName.tag.toRegex(), "")
            }
            if (!isToMultiple) {
                if (companyDomain != null && companyDomain.isNotEmpty()) {
                    val url = "https://$companyDomain?barcode=$barcode"
                    template = template?.replace(
                        SmsTemplateTag.shareLocationUrl.tag.toRegex(),
                        url
                    )
                } else {
                    template = template?.replace(
                        " " + SmsTemplateTag.shareLocationUrl.tag.toRegex(),
                        ""
                    )
                    template = template?.replace(
                        SmsTemplateTag.shareLocationUrl.tag.toRegex(),
                        ""
                    )
                }
            } else {
                template = template?.replace(
                    " " + SmsTemplateTag.shareLocationUrl.tag.toRegex(),
                    ""
                )
                template = template?.replace(
                    SmsTemplateTag.shareLocationUrl.tag.toRegex(),
                    ""
                )
            }
            if (driverName != null && driverName.isNotEmpty()) {
                template = template?.replace(
                    SmsTemplateTag.driverName.tag.toRegex(),
                    driverName
                )
            } else {
                template = template?.replace(
                    " " + SmsTemplateTag.driverName.tag.toRegex(),
                    ""
                )
                template =
                    template?.replace(SmsTemplateTag.driverName.tag.toRegex(), "")
            }
            if (driverPhone != null && !driverPhone.isEmpty()) {
                template = template?.replace(
                    SmsTemplateTag.driverPhone.tag.toRegex(),
                    driverPhone
                )
            } else {
                template = template?.replace(
                    " " + SmsTemplateTag.driverPhone.tag.toRegex(),
                    ""
                )
                template =
                    template?.replace(SmsTemplateTag.driverPhone.tag.toRegex(), "")
            }
            if (company != null && !company.isEmpty()) {
                template =
                    template?.replace(SmsTemplateTag.company.tag.toRegex(), company)
            } else {
                template =
                    template?.replace(" " + SmsTemplateTag.company.tag.toRegex(), "")
                template = template?.replace(SmsTemplateTag.company.tag.toRegex(), "")
            }
            if (cod != null && cod.isNotEmpty()) {
                template = template?.replace(SmsTemplateTag.cod.tag.toRegex(), cod)
            } else {
                template =
                    template?.replace(" " + SmsTemplateTag.cod.tag.toRegex(), "")
                template = template?.replace(SmsTemplateTag.cod.tag.toRegex(), "")
            }
            if (postponeDate != null && postponeDate.isNotEmpty()) {
                template = template?.replace(
                    SmsTemplateTag.postponeDate.tag.toRegex(),
                    postponeDate
                )
            } else {
                template = template?.replace(
                    " " + SmsTemplateTag.postponeDate.tag.toRegex(),
                    ""
                )
                template =
                    template?.replace(SmsTemplateTag.postponeDate.tag.toRegex(), "")
            }
            if (!expectedDeliveryDate.isEmpty()) {
                template = template?.replace(
                    SmsTemplateTag.expectedDeliveryDate.tag.toRegex(),
                    expectedDeliveryDate
                )
            } else {
                template = template?.replace(
                    " " + SmsTemplateTag.expectedDeliveryDate.tag.toRegex(),
                    ""
                )
                template = template?.replace(
                    SmsTemplateTag.expectedDeliveryDate.tag.toRegex(),
                    ""
                )
            }
            if (hubName.isNotEmpty()) {
                template = template?.replace(
                    SmsTemplateTag.postponeDate.tag.toRegex(),
                    hubName
                )
            } else {
                template =
                    template?.replace(" " + SmsTemplateTag.hubName.tag.toRegex(), "")
                template = template?.replace(SmsTemplateTag.hubName.tag.toRegex(), "")
            }
            return template
        }

        fun removeDuplicates(list: ArrayList<String?>): ArrayList<String?> {
            val newList = ArrayList<String?>()
            for (element in list) {
                if (!newList.contains(element)) {
                    newList.add(element)
                }
            }
            return newList
        }

        fun isMinVersionHigher(minVersion: String, context: Context): Boolean {
            val minVersionArray = minVersion.split(".")
            val currentVersionArray = getAppVersion(context).split(".")

            val splitMinVersion = java.lang.StringBuilder()
            val splitCurrentVersion = java.lang.StringBuilder()

            return if (minVersionArray.size == 3 && currentVersionArray.size == 3) {
                for (index in 0..2) {
                    splitMinVersion.append(minVersionArray[index])
                    splitCurrentVersion.append(currentVersionArray[index])
                }
                splitCurrentVersion.toString().toInt() < splitMinVersion.toString().toInt()
            } else {
                true
            }
        }

        private fun getAppVersion(context: Context): String {
            return try {
                val pInfo = context.packageManager.getPackageInfo(
                    context.packageName, 0
                )
                pInfo.versionName

            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                "0.0.0"
            }
        }

        fun formatServerDateLocalized(source: String?, format: DateFormats): String {
            if (source != null) {
                val parser = SimpleDateFormat(DateFormats.SERVER_FORMAT.value, Locale.US)

                val formatter: SimpleDateFormat =
                    if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                        SimpleDateFormat(format.value, Locale("ar"))
                    } else {
                        SimpleDateFormat(format.value, Locale.US)
                    }

                return formatter.format(parser.parse(source)!!)
            }
            return ""
        }

        //Attachments handling
        @Throws(IOException::class)
        fun createImageFile(mActivity: Activity): File? {
            // Create an image file name
            val timeStamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale("en"))
                    .format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"

            return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                mActivity.filesDir /* directory */
            )
        }

        fun isStorageAndCameraPermissionNeeded(mActivity: Activity): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(
                    mActivity.applicationContext,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    mActivity.applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    mActivity.applicationContext,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            }
        }


        fun showAndRequestCameraAndStorageDialog(mActivity: Activity?) {
            ActivityCompat.requestPermissions(
                mActivity!!,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                AppConstants.REQUEST_CAMERA_AND_STORAGE_PERMISSION
            )
        }

        fun showAndRequestCameraAndStorageDialog(mFragment: Fragment) {
            mFragment.requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                AppConstants.REQUEST_CAMERA_AND_STORAGE_PERMISSION
            )
        }

        fun shouldShowCameraAndStoragePermissionDialog(mActivity: Activity?): Boolean {
            return if (mActivity != null) {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    mActivity,
                    Manifest.permission.CAMERA
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    mActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } else {
                false
            }
        }

        fun isStoragePermissionNeeded(mActivity: Activity): Boolean {
            return ContextCompat.checkSelfPermission(
                mActivity.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        }

        fun showAndRequestStorageDialog(mActivity: Activity?) {
            return ActivityCompat.requestPermissions(
                mActivity!!,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                AppConstants.REQUEST_STORAGE_PERMISSION
            )
        }

        fun shouldShowStoragePermissionDialog(mActivity: Activity?): Boolean {
            return if (mActivity != null) {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    mActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } else {
                false
            }
        }

        fun isCameraPermissionNeeded(mActivity: Activity): Boolean {
            return ContextCompat.checkSelfPermission(
                mActivity.applicationContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        }


        fun showAndRequestCameraDialog(mActivity: Activity?) {
            ActivityCompat.requestPermissions(
                mActivity!!,
                arrayOf(Manifest.permission.CAMERA),
                AppConstants.REQUEST_CAMERA_PERMISSION
            )
        }

        fun shouldShowCameraPermissionDialog(mActivity: Activity?): Boolean {
            return if (mActivity != null) {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    mActivity,
                    Manifest.permission.CAMERA
                )
            } else {
                false
            }
        }

        fun validateCompressedImage(
            imageUri: Uri,
            doesDelete: Boolean,
            context: Context?
        ): LoadedImage? {
            val compressedImageUri = LoadedImage()
            val mActualFile: File =
                File(getRealPathFromURI(context, imageUri) ?: "")
            val mActualFileLength = mActualFile.length() * 1.0 / 1024
            try {
                val compressedFileName = "_" + mActualFile.name
                val compressedUri =
                    Uri.fromFile(
                        Compressor(context).compressToFile(
                            mActualFile,
                            compressedFileName
                        )
                    )
                compressedImageUri.imageUri = compressedUri
            } catch (e: IOException) {
                val convertImageToFileAndroid10: File? =
                    convertImageToFileAndroid10(context, imageUri)
                compressedImageUri.imageUri = Uri.fromFile(convertImageToFileAndroid10)
            }
            if (doesDelete) {
                if (mActualFile.exists()) {
                    mActualFile.delete()
                }
            }

            return if (compressedImageUri.imageUri != null) {
                compressedImageUri
            } else {
                null
            }
        }


        fun getRealPathFromURI(context: Context?, uri: Uri): String? {
            if (context != null) {
                val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

                // DocumentProvider
                if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                    // ExternalStorageProvider
                    if (isExternalStorageDocument(uri)) {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":".toRegex()).toTypedArray()
                        val type = split[0]
                        if ("primary".equals(type, ignoreCase = true)) {
                            return Environment.getExternalStorageDirectory()
                                .toString() + "/" + split[1]
                        }

                        // TODO handle non-primary volumes
                    } else if (isDownloadsDocument(uri)) {
                        val id = DocumentsContract.getDocumentId(uri)
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                        )
                        return getDataColumn(
                            context,
                            contentUri,
                            null,
                            null
                        )
                    } else if (isMediaDocument(uri)) {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":".toRegex()).toTypedArray()
                        val type = split[0]
                        var contentUri: Uri? = null
                        if ("image" == type) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == type) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == type) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(
                            split[1]
                        )
                        return getDataColumn(
                            context,
                            contentUri,
                            selection,
                            selectionArgs
                        )
                    }
                } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                    // Return the remote address
                    return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                        context,
                        uri,
                        null,
                        null
                    )
                } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                    return uri.path
                }
                return null
            } else {
                return null
            }
        }

        fun convertImageToFileAndroid10(context: Context?, imageUri: Uri?): File? {
            var bitmap: Bitmap? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    bitmap = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context?.contentResolver!!,
                            imageUri!!
                        )
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return if (bitmap != null) {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 5, baos) //bm is the bitmap object
                val mTempByteArray = baos.toByteArray()
                try {
                    val mRealFile: File =
                        createImageFileForCompressedMedia(context)
                    val mFileInputStream = FileOutputStream(mRealFile)
                    mFileInputStream.write(mTempByteArray)
                    mFileInputStream.flush()
                    mFileInputStream.close()
                    mRealFile
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }
        }

        @Throws(IOException::class)
        fun createImageFileForCompressedMedia(context: Context?): File {
            // Create an image file name
            val timeStamp =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale("en"))
                    .format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                File(context?.cacheDir?.path + File.separator + "images") /* directory */
            )
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        private fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }


        fun writeTextOnDrawable(
            context: Context?,
            resources: Resources,
            drawableId: Int,
            text: String
        ): Bitmap {
            val bm = drawableToBitmap(
                ContextCompat.getDrawable(
                    context!!,
                    drawableId
                )!!
            )?.copy(Bitmap.Config.ARGB_8888, true)
            val tf = Typeface.create("Helvetica", Typeface.BOLD)
            val paint = Paint()
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            paint.typeface = tf
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = convertToPixels(context, 14).toFloat()
            val textRect = Rect()
            paint.getTextBounds(text, 0, text.length, textRect)
            val canvas = Canvas(bm!!)

            //If the text is bigger than the canvas , reduce the font size
            if (textRect.width() >= canvas.width - 4) //the padding on either sides is considered as 4, so as to appropriately fit in the text
                paint.textSize =
                    convertToPixels(
                        context,
                        7
                    ).toFloat() //Scaling needs to be used for different dpi's

            //Calculate the positions
            val xPos = canvas.width / 2 - 2 //-2 is for regulating the x position offset

            //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
            val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt() - 12
            canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), paint)
            return bm
        }

        private fun convertToPixels(context: Context?, nDP: Int): Int {
            val conversionScale = context!!.resources.displayMetrics.density
            return (nDP * conversionScale + 0.5f).toInt()
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        fun decodeMapGeometry(encodedPath: String): List<LatLng> {
            val len = encodedPath.length

            // For speed we preallocate to an upper bound on the final length, then
            // truncate the array before returning.
            val path: MutableList<LatLng> = ArrayList()
            var index = 0
            var lat = 0
            var lng = 0
            while (index < len) {
                var result = 1
                var shift = 0
                var b: Int
                do {
                    b = encodedPath[index++].code - 63 - 1
                    result += b shl shift
                    shift += 5
                } while (b >= 0x1f)
                lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
                result = 1
                shift = 0
                do {
                    b = encodedPath[index++].code - 63 - 1
                    result += b shl shift
                    shift += 5
                } while (b >= 0x1f)
                lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
                path.add(LatLng(lat * 1e-5, lng * 1e-5))
            }
            return path
        }
    }
}