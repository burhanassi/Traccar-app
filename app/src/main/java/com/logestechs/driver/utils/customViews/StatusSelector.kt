package com.logestechs.driver.utils.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StyleableRes
import androidx.core.content.ContextCompat
import com.logestechs.driver.R
import com.logestechs.driver.databinding.ViewStatusSelectorBinding

class StatusSelector : FrameLayout {
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    @StyleableRes
    val textStatus = 0

    var isStatusSelected = false

    lateinit var textView: TextView
    lateinit var container: FrameLayout

    var enumValue: Any? = null

    private var _binding: ViewStatusSelectorBinding? = null
    private val binding get() = _binding!!

    private fun init(attrs: AttributeSet?) {
        _binding = ViewStatusSelectorBinding.inflate(LayoutInflater.from(context), this)

        textView = binding.textStatus
        container = binding.frameContainer
        val sets = intArrayOf(R.attr.textStatus)
        val typedArray = context.obtainStyledAttributes(attrs, sets)

        textView.text = typedArray.getText(textStatus)?.toString()

        typedArray.recycle()

    }

    fun handleSelection() {
        if (isStatusSelected) {
            isStatusSelected = false
            container.background =
                ContextCompat.getDrawable(context, R.drawable.background_unselected_shipment_status)

            textView.setTextColor(ContextCompat.getColor(context, R.color.fontTitleOrange))

        } else {
            isStatusSelected = true
            container.background =
                ContextCompat.getDrawable(context, R.drawable.background_selected_shipment_status)

            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    fun makeUnselected() {
        isStatusSelected = false
        container.background =
            ContextCompat.getDrawable(context, R.drawable.background_unselected_shipment_status)
        textView.setTextColor(ContextCompat.getColor(context, R.color.fontTitleOrange))

    }

    fun makeSelected() {
        isStatusSelected = true
        container.background =
            ContextCompat.getDrawable(context, R.drawable.background_selected_shipment_status)
        textView.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    fun setTextStatus(text: CharSequence?) {
        textView.text = text
    }

}