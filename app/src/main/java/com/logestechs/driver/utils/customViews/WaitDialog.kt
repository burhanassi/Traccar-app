package com.logestechs.driver.utils.customViews

import android.app.Activity
import android.app.AlertDialog
import android.content.ContextWrapper
import android.view.LayoutInflater
import com.logestechs.driver.R


class WaitDialog(activity: Activity?) {
    private var alertDialog: AlertDialog? = null
    private var activity: Activity? = null

    fun showDialog() {
        if (activity == null) {
            return
        }
        if (alertDialog != null && !alertDialog!!.isShowing) {
            if (activity != null) {
                if (activity?.isFinishing == false) {
                    alertDialog?.show()
                }
            }
        }
    }

    fun dismissDialog() {
        if (activity == null) {
            return
        }
        if (alertDialog != null && alertDialog?.isShowing == true) {
            val context = (alertDialog?.context as ContextWrapper).baseContext
            if (context is Activity) {
                if (!context.isFinishing) {
                    alertDialog?.dismiss()
                }
            } else {
                alertDialog?.dismiss()
            }
        }
    }

    init {
        this.activity = activity
        alertDialog = AlertDialog.Builder(activity, R.style.AlertDialogStyle)
            .setCancelable(false)
            .setView(LayoutInflater.from(activity).inflate(R.layout.dialog_wait, null))
            .create()
    }
}

