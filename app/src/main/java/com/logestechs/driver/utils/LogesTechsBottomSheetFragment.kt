package com.logestechs.driver.utils

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class LogesTechsBottomSheetFragment : BottomSheetDialogFragment() {
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
}