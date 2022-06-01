package com.logestechs.driver.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.logestechs.driver.utils.customViews.WaitDialog
import java.lang.ref.WeakReference


abstract class LogesTechsActivity : AppCompatActivity() {
    private var mWaitDialog: WaitDialog? = null

    fun showWaitDialog() {
        if (!this.isFinishing) {
            if (mWaitDialog == null) {
                mWaitDialog = WaitDialog(this)
            }
            mWaitDialog!!.showDialog()
        }
    }

    fun hideWaitDialog() {
        mWaitDialog?.dismissDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogesTechsApp.instance.currentActivity = WeakReference(this)
    }
}