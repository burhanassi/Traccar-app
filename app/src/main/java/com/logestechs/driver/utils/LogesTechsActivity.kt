package com.logestechs.driver.utils

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.logestechs.driver.utils.customViews.WaitDialog
import com.yariksoffice.lingver.Lingver
import java.lang.ref.WeakReference


abstract class LogesTechsActivity : AppCompatActivity() {
    private var mWaitDialog: WaitDialog? = null

    fun showWaitDialog() {
        if (!this.isFinishing) {
            if (mWaitDialog == null) {
                mWaitDialog = WaitDialog(this)
            }
            mWaitDialog?.showDialog()
        }
    }

    fun hideWaitDialog() {
        mWaitDialog?.dismissDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogesTechsApp.instance.currentActivity = WeakReference(this)
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
}