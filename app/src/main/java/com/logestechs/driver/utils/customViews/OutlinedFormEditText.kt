package com.logestechs.driver.utils.customViews

import android.annotation.SuppressLint
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
import com.logestechs.driver.databinding.ViewOutlinedFormEditTextBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp


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

    @SuppressLint("ResourceType")
    @StyleableRes
    val imageStart = 1

    @SuppressLint("ResourceType")
    @StyleableRes
    val isDropdown = 2

    @SuppressLint("ResourceType")
    @StyleableRes
    val isNumeric = 3

    @SuppressLint("ResourceType")
    @StyleableRes
    val isPhone = 4

    lateinit var editText: EditText
    lateinit var textView: TextView
    lateinit var formButton: LinearLayout
    lateinit var iconImageView: ImageView
    lateinit var viewLine: View

    var isValid: Boolean = true

    private var _binding: ViewOutlinedFormEditTextBinding? = null
    private val binding get() = _binding!!

    private fun init(attrs: AttributeSet?) {

        _binding = ViewOutlinedFormEditTextBinding.inflate(LayoutInflater.from(context), this)

        val sets =
            intArrayOf(
                R.attr.hintText,
                R.attr.imageStart,
                R.attr.isDropdown,
                R.attr.isNumeric,
                R.attr.isPhone
            )
        val typedArray = context.obtainStyledAttributes(attrs, sets)

        editText = binding.editText
        textView = binding.textView
        editText.isSaveEnabled = false
        viewLine = binding.viewLine
        formButton = binding.linearLayoutIconsContainer
        iconImageView = binding.imageViewIcon


        if (typedArray.getDrawable(imageStart) != null) {
            iconImageView.setImageDrawable(typedArray.getDrawable(imageStart))
        } else {
            binding.linearLayoutIconsContainer.visibility = View.GONE
        }

        if (typedArray.getBoolean(isDropdown, false)) {
            binding.icArrow.visibility = View.VISIBLE
            iconImageView.setPadding(6, 4, 6, 4)
        }

        binding.textView.text = typedArray.getText(textHint)?.toString()

        if (typedArray.getBoolean(isNumeric, false)) {
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }


        if (typedArray.getBoolean(isPhone, false)) {
            editText.inputType = InputType.TYPE_CLASS_PHONE
        }

        typedArray.recycle()

    }

    fun makeInvalid() {
        isValid = false
        binding.linearLayoutContainer.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext_red)
        binding.textView.setTextColor(ContextCompat.getColor(context, R.color.selectedBottomBarItemTint))
    }

    fun makeValid() {
        isValid = true
        binding.linearLayoutContainer.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext)

        binding.textView.setTextColor(ContextCompat.getColor(context, R.color.fontTitleBlack))
    }

    fun getText(): String {
        return binding.editText.text.toString()
    }

    fun getHintText(): String {
        return textView.text.toString()
    }

    fun setText(text: String?) {
        if (binding.editText != null) {
            binding.editText.setText(text ?: "")
        }
    }

    fun clearEditTextFocus() {
        binding.editText.clearFocus()
    }

    fun clearText() {
        editText.setText("")
    }

    fun disableInput() {
        binding.linearLayoutContainer.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_disabled_edittext)
        binding.textView.setTextColor(ContextCompat.getColor(context, R.color.fontTitleBlack))
        editText.isEnabled = false
    }

    fun enableInput() {
        binding.linearLayoutContainer.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext)
        editText.isEnabled = true
    }

    fun isEmpty(): Boolean {
        return editText.text.toString().trim().isEmpty()
    }

    fun setBorderFillerColor(color: Int) {
        viewLine.setBackgroundColor(ContextCompat.getColor(LogesTechsApp.instance, color))
    }

    fun setCurrency() {
        binding.imageViewIcon.visibility = View.GONE
        binding.textCurrency.visibility = View.VISIBLE
        binding.textCurrency.text = Helper.getCompanyCurrency()
    }
}