package com.logestechs.traccarApp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.logestechs.traccarApp.utils.customViews.WaitDialog
import com.yariksoffice.lingver.Lingver
import java.lang.ref.WeakReference


abstract class LogesTechsActivity : AppCompatActivity() {
    private var mWaitDialog: WaitDialog? = null
    private var tempMobileNumber: String? = null
    var currentLangCode: String? = null

    private var telephonyManager: TelephonyManager? = null
    private var phoneStateListener: CustomPhoneStateListener? = null

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
        checkPermissions()
        handleForwardNavigationAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
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

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                AppConstants.REQUEST_READ_PHONE_STATE
            )
        } else {
            initializeTelephonyOperations()
        }
    }

    private fun initializeTelephonyOperations() {
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        phoneStateListener = CustomPhoneStateListener()
        telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
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

            AppConstants.REQUEST_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeTelephonyOperations()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message to the user)
                }
            }
        }
    }
}
