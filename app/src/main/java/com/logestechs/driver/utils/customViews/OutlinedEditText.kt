package com.logestechs.driver.utils.customViews

import android.annotation.SuppressLint
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
import com.logestechs.driver.databinding.ViewOutlinedEditTextBinding
import com.logestechs.driver.utils.AppLanguages
import com.yariksoffice.lingver.Lingver

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

    @SuppressLint("ResourceType")
    @StyleableRes
    val imageStart = 1

    @SuppressLint("ResourceType")
    @StyleableRes
    val isPassword = 2

    lateinit var editText: EditText
    lateinit var buttonShowPassword: EditText

    private var _binding: ViewOutlinedEditTextBinding? = null
    private val binding get() = _binding!!
    private fun init(attrs: AttributeSet?) {
        _binding = ViewOutlinedEditTextBinding.inflate(LayoutInflater.from(context), this)

        val sets = intArrayOf(R.attr.hintText, R.attr.imageStart, R.attr.isPassword)
        val typedArray = context.obtainStyledAttributes(attrs, sets)

        editText = binding.editText

        if (typedArray.getBoolean(isPassword, false)) {

            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                binding.editText.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, typedArray.getDrawable(imageStart), null
                )
            } else {
                binding.editText.setCompoundDrawablesWithIntrinsicBounds(
                    typedArray.getDrawable(imageStart), null, null, null
                )
            }
            editText.inputType =
                (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        } else {

            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {

                binding.editText.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, typedArray.getDrawable(
                        imageStart
                    ), null
                )
            } else {
                binding.editText.setCompoundDrawablesWithIntrinsicBounds(
                    typedArray.getDrawable(
                        imageStart
                    ), null, null, null
                )
            }
        }

        binding.textView.text = typedArray.getText(textHint)?.toString()

        typedArray.recycle()

    }

    fun makeInvalid() {
        binding.textView.setTextColor(ContextCompat.getColor(context, R.color.selectedBottomBarItemTint))

        for (drawable in binding.editText.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.selectedBottomBarItemTint),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }

        binding.editText.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext_red)

        binding.editText.setTextColor(ContextCompat.getColor(context, R.color.selectedBottomBarItemTint))

    }

    fun makeValid() {
        binding.textView.setTextColor(ContextCompat.getColor(context, R.color.fontTitleBlack))

        for (drawable in binding.editText.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(context, R.color.borderOutlinedEditText),
                        PorterDuff.Mode.SRC_IN
                    )
            }
        }

        binding.editText.background =
            ContextCompat.getDrawable(context, R.drawable.border_outlined_edittext)

        binding.editText.setTextColor(ContextCompat.getColor(context, R.color.fontTitleBlack))

    }

    fun getText(): String {
        return binding.editText.text.toString()
    }

    fun isEmpty(): Boolean {
        return editText.text.trim().isEmpty()
    }

}