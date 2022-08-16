package com.logestechs.driver.utils.customViews

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat
import com.logestechs.driver.R
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import kotlinx.android.synthetic.main.view_outlined_form_edit_text.view.*


class OutlinedFormEditText : RelativeLayout {
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
    val isDropdown = 2

    @StyleableRes
    val isNumeric = 3

    lateinit var editText: EditText
    lateinit var textView: TextView
    lateinit var formButton: LinearLayout
    lateinit var iconImageView: ImageView
    lateinit var viewLine: View

    var isValid: Boolean = true

    private fun init(attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.view_outlined_form_edit_text, this, true)

        val sets =
            intArrayOf(R.attr.hintText, R.attr.imageStart, R.attr.isDropdown, R.attr.isNumeric)
        val typedArray = context.obtainStyledAttributes(attrs, sets)

        editText = edit_text
        textView = text_view
        editText.isSaveEnabled = false
        viewLine = view_line
        formButton = linear_Layout_icons_container
        iconImageView = image_view_icon


        if (typedArray.getDrawable(imageStart) != null) {
            iconImageView.setImageDrawable(typedArray.getDrawable(imageStart))
        } else {
            linear_Layout_icons_container.visibility = View.GONE
        }

        if (typedArray.getBoolean(isDropdown, false)) {
            ic_arrow.visibility = View.VISIBLE
            iconImageView.setPadding(6, 4, 6, 4)
        }

        text_view.text = typedArray.getText(textHint)?.toString()

        if (typedArray.getBoolean(isNumeric, false)) {
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        typedArray.recycle()

    }

    fun makeInvalid() {
        isValid = false
        linear_Layout_container.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext_red)
        text_view.setTextColor(ContextCompat.getColor(context, R.color.selectedBottomBarItemTint))
    }

    fun makeValid() {
        isValid = true
        linear_Layout_container.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext)

        text_view.setTextColor(ContextCompat.getColor(context, R.color.fontTitleBlack))
    }

    fun isEmpty(): Boolean {
        return editText.text.isNullOrEmpty()
    }

    fun getText(): String {
        return edit_text.text.toString()
    }

    fun getHintText(): String {
        return textView.text.toString()
    }

    fun setText(text: String?) {
        if (edit_text != null) {
            edit_text.setText(text ?: "")
        }
    }

    fun clearEditTextFocus() {
        edit_text.clearFocus()
    }

    fun clearText() {
        editText.setText("")
    }

    fun disableInput() {
        linear_Layout_container.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_disabled_edittext)
        text_view.setTextColor(ContextCompat.getColor(context, R.color.fontTitleBlack))
        editText.isEnabled = false
    }

    fun enableInput() {
        linear_Layout_container.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext)
        editText.isEnabled = true
    }


    fun setBorderFillerColor(color: Int) {
        viewLine.setBackgroundColor(ContextCompat.getColor(LogesTechsApp.instance, color))
    }

    fun setCurrency() {
        image_view_icon.visibility = View.GONE
        text_currency.visibility = View.VISIBLE
        text_currency.text = Helper.getCompanyCurrency()
    }
}