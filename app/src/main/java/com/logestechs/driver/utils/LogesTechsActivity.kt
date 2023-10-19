package com.logestechs.driver.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Address
import com.logestechs.driver.databinding.DialogConfirmActionBinding
import com.logestechs.driver.utils.bottomSheets.NotificationsBottomSheet
import com.logestechs.driver.utils.bottomSheets.PackageTrackBottomSheet
import com.logestechs.driver.utils.customViews.WaitDialog
import com.logestechs.driver.utils.interfaces.ConfirmationDialogActionListener
import com.yariksoffice.lingver.Lingver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun showConfirmationDialog(
        message: String,
        input: Any?,
        action: ConfirmationDialogAction,
        listener: ConfirmationDialogActionListener
    ) {
        val dialogBuilder = AlertDialog.Builder(this, 0)
        val binding: DialogConfirmActionBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                this
            ), R.layout.dialog_confirm_action, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()

        binding.textMessage.text = message

        binding.buttonConfirm.setOnClickListener {
            listener.confirmAction(input, action)
            alertDialog.dismiss()
        }

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.show()
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
        var number: String? = mobileNumber
        if (mobileNumber != null && mobileNumber.trim().isNotEmpty()) {
            if (Helper.getCompanyCurrency() == AppCurrency.SAR.value) {
                number = Helper.formatNumberForWhatsApp(
                    mobileNumber,
                    false
                )
            }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
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
                    tempMobileNumber = number
                }
            } else {
                this.startActivity(intent)
            }
        }
    }

    fun sendSms(mobileNumber: String?, messageText: String?) {
        var number: String? = mobileNumber
        if (Helper.getCompanyCurrency() == AppCurrency.SAR.value) {
            number = Helper.formatNumberForWhatsApp(
                mobileNumber,
                false
            )
        }
        val uri = Uri.parse("smsto:$number")
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
            if (SharedPreferenceWrapper.getIsWhatsappBusiness()) {
                intent.setPackage("com.whatsapp.w4b")
            } else {
                intent.setPackage("com.whatsapp")
            }
            if (intent.resolveActivity(packageManager) != null) {
                this.startActivity(intent)
            } else {
                Helper.showErrorMessage(this, getString(R.string.error_install_whatsapp))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Helper.showErrorMessage(this, getString(R.string.error_install_whatsapp))
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

    fun getNotifications() {
        showWaitDialog()
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.getNotifications()
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response!!.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        val bottomSheet = NotificationsBottomSheet()
                        val bundle = Bundle()
                        bundle.putParcelableArrayList(
                            BundleKeys.NOTIFICATIONS_KEY.toString(),
                            data.notificationsList
                        )

                        bundle.putInt(
                            BundleKeys.UNREAD_NOTIFICATIONS_COUNT.toString(),
                            data.totalRecordsNo
                        )
                        bottomSheet.arguments = bundle
                        bottomSheet.show(supportFragmentManager, "exampleBottomSheet")

                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@LogesTechsActivity,
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@LogesTechsActivity,
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(this@LogesTechsActivity, e.message)
                        } else {
                            Helper.showErrorMessage(this@LogesTechsActivity, e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                this, getString(R.string.error_check_internet_connection)
            )
        }
    }

    fun trackShipmentNotification(packageId: Long) {
        showWaitDialog()
        if (Helper.isInternetAvailable(this)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = ApiAdapter.apiClient.trackPackageDriverNotification(packageId)
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response!!.isSuccessful && response.body() != null) {
                        val data = response?.body()!!
                        val bottomSheet = PackageTrackBottomSheet()
                        val bundle = Bundle()
                        bundle.putParcelable(BundleKeys.PKG_KEY.toString(), data)
                        bottomSheet.arguments = bundle
                        bottomSheet.show(supportFragmentManager, "exampleBottomSheet")

                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@LogesTechsActivity,
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    this@LogesTechsActivity,
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(this@LogesTechsActivity, e.message)
                        } else {
                            Helper.showErrorMessage(
                                this@LogesTechsActivity,
                                e.stackTraceToString()
                            )
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                this, getString(R.string.error_check_internet_connection)
            )
        }
    }

}
