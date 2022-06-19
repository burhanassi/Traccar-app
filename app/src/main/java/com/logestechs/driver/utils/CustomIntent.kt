package com.logestechs.driver.utils

import android.app.Activity
import android.content.Context
import com.logestechs.driver.R


class CustomIntent {
    companion object {
        fun customType(context: Context, animType: String?) {
            val act = context as Activity
            when (animType) {
                "left-to-right" -> act.overridePendingTransition(
                    R.anim.push_left_in,
                    R.anim.push_left_out
                )
                "right-to-left" -> act.overridePendingTransition(
                    R.anim.left_to_right,
                    R.anim.right_to_left
                )
                else -> {}
            }
        }
    }
}
