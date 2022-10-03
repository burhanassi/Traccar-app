package com.logestechs.driver.utils.customViews

import android.annotation.TargetApi
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat
import com.logestechs.driver.R
import com.logestechs.driver.utils.AppLanguages
import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.view_outlined_edit_text.view.*

class OutlinedEditText : LinearLayout {
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    @StyleableRes
    val textHint = 0

    @StyleableRes
    val imageStart = 1

    @StyleableRes
    val isPassword = 2

    lateinit var editText: EditText
    lateinit var buttonShowPassword: EditText

    private fun init(attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.view_outlined_edit_text, this, true)

        val sets = intArrayOf(R.attr.hintText, R.attr.imageStart, R.attr.isPassword)
        val typedArray = context.obtainStyledAttributes(attrs, sets)

        editText = edit_text

        if (typedArray.getBoolean(isPassword, false)) {

            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                edit_text.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, typedArray.getDrawable(imageStart), null
                )
            } else {
                edit_text.setCompoundDrawablesWithIntrinsicBounds(
                    typedArray.getDrawable(imageStart), null, null, null
                )
            }
            editText.inputType =
                (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        } else {

            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {

                edit_text.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, typedArray.getDrawable(
                        imageStart
                    ), null
                )
            } else {
                edit_text.setCompoundDrawablesWithIntrinsicBounds(
                    typedArray.getDrawable(
                        imageStart
                    ), null, null, null
                )
            }
        }

        text_view.text = typedArray.getText(textHint)?.toString()

        typedArray.recycle()

    }

    fun makeInvalid() {
        text_view.setTextColor(ContextCompat.getColor(context, R.color.selectedBottomBarItemTint))

        for (drawable in edit_text.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.selectedBottomBarItemTint),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }

        edit_text?.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext_red)

        edit_text.setTextColor(ContextCompat.getColor(context, R.color.selectedBottomBarItemTint))

    }

    fun makeValid() {
        text_view.setTextColor(ContextCompat.getColor(context, R.color.fontTitleBlack))

        for (drawable in edit_text.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.borderOutlinedEditText),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }

        edit_text?.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext)

        edit_text.setTextColor(ContextCompat.getColor(context, R.color.fontTitleBlack))

    }

    fun getText(): String {
        return edit_text.text.toString()
    }

    fun isEmpty(): Boolean {
        return editText.text.trim().isEmpty()
    }

}