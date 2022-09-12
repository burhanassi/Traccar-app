package com.logestechs.driver.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Address
import com.logestechs.driver.utils.customViews.WaitDialog
import com.yariksoffice.lingver.Lingver
import java.lang.ref.WeakReference
import java.net.URLEncoder


abstract class LogesTechsActivity : AppCompatActivity() {
    private var mWaitDialog: WaitDialog? = null
    private var tempMobileNumber: String? = null
    var currentLangCode: String? = null

    fun showWaitDialog() {
        if (!this.isFinishing) {
            if (mWaitDialog == null) {
                mWaitDialog = WaitDialog(this)
            }
            mWaitDialog?.showDialog()
        }
    }

    open fun hideWaitDialog() {
        mWaitDialog?.dismissDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogesTechsApp.instance.currentActivity = WeakReference(this)
        currentLangCode = Lingver.getInstance().getLocale().toString()
        handleForwardNavigationAnimation()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        handleBackwardNavigationAnimation()
    }

    fun hideStatusBar() {
        val decorView = window.decorView
        val uiOptions =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = uiOptions
    }

    private fun handleForwardNavigationAnimation() {
        if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
            CustomIntent.customType(this, IntentAnimation.RTL.value)
        } else {
            CustomIntent.customType(this, IntentAnimation.LTR.value)
        }
    }

    private fun handleBackwardNavigationAnimation() {
        if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
            CustomIntent.customType(this, IntentAnimation.LTR.value)
        } else {
            CustomIntent.customType(this, IntentAnimation.RTL.value)
        }
    }

    fun getContext(): Context {
        return this
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            AppConstants.PERMISSIONS_REQUEST_PHONE_CALL -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    callMobileNumber(tempMobileNumber)
                } else {
                    Helper.showErrorMessage(
                        this,
                        getString(R.string.error_phone_permission)
                    )
                }
                tempMobileNumber = null
            }
        }

    }

    fun callMobileNumber(mobileNumber: String?) {
        if (mobileNumber != null && mobileNumber.trim().isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobileNumber"))

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(
                        arrayOf(Manifest.permission.CALL_PHONE),
                        AppConstants.PERMISSIONS_REQUEST_PHONE_CALL
                    )
                    tempMobileNumber = mobileNumber
                }
            } else {
                this.startActivity(intent)
            }
        }
    }

    fun sendSms(mobileNumber: String?, messageText: String?) {
        val uri = Uri.parse("smsto:$mobileNumber")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", messageText)
        startActivity(intent)
    }

    fun sendSmsToMultiple(mobileNumbers: ArrayList<String?>, messageText: String?) {
        val numbersString: StringBuilder = java.lang.StringBuilder()
        numbersString.append("smsto:")
        for (number in mobileNumbers) {
            numbersString.append(number)
            numbersString.append(";")
        }
        val uri = Uri.parse(numbersString.toString())
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", messageText)
        startActivity(intent)
    }

    fun sendWhatsAppMessage(mobileNumber: String?, messageText: String?) {
        val packageManager: PackageManager = this.packageManager
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            val url =
                "https://api.whatsapp.com/send?phone=" + Helper.replaceArabicNumbers(
                    mobileNumber ?: ""
                )
                    .toString() + "&text=" + URLEncoder.encode(
                    messageText,
                    "UTF-8"
                )
            intent.data = Uri.parse(url)
            if (intent.resolveActivity(packageManager) != null) {
                this.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showLocationInGoogleMaps(address: Address?) {
        val packageManager: PackageManager = this.packageManager
        val intent = Intent(Intent.ACTION_VIEW)
        try {
            val locationDirection: String = Helper.getGoogleNavigationUrl(
                address?.latitude,
                address?.longitude
            )
            intent.data = Uri.parse(locationDirection)
            if (intent.resolveActivity(packageManager) != null) {
                this.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
