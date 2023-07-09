package com.logestechs.driver.ui.barcodeScanner

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.logestechs.driver.R
import com.logestechs.driver.databinding.FragmentSetTimeSpentBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SetTimeSpent : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentSetTimeSpentBinding
    private var hours: Double? = null
    private var dataListener: DataListener? = null
    private var isUpdating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetTimeSpentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editTextNumberHours.inputType = InputType.TYPE_CLASS_NUMBER

        val maxLength = 5
        val inputFilter = InputFilter.LengthFilter(maxLength)
        binding.editTextNumberHours.filters = arrayOf(inputFilter)

        binding.editTextNumberHours.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No implementation needed
            }

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) {
                    return
                }

                val text = s.toString()
                if (text.length == 2 && !text.contains(":")) {
                    val formattedText = "$text:"
                    isUpdating = true
                    binding.editTextNumberHours.setText(formattedText)
                    binding.editTextNumberHours.setSelection(formattedText.length)
                    isUpdating = false
                }

                val isValidTimeFormat = isValidTimeFormat(text)
                binding.buttonDoneFragment.isEnabled = isValidTimeFormat
            }
        })
        val initialText = binding.editTextNumberHours.text.toString()
        binding.buttonDoneFragment.isEnabled = isValidTimeFormat(initialText)
        binding.buttonDoneFragment.setOnClickListener(this)
        binding.buttonCancel.setOnClickListener(this)
    }
    override fun onStart() {
        super.onStart()

        val dialogWidth = resources.displayMetrics.widthPixels * 0.7f
        val dialogHeight = resources.displayMetrics.heightPixels * 0.25f

        dialog?.window?.setLayout(dialogWidth.toInt(), dialogHeight.toInt())

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_done_fragment -> {
                hours = getTimeAsDoubleFromText(binding.editTextNumberHours.text.toString())
                dataListener?.onDataReceived(hours)
                dismiss()
                (activity as? FulfilmentSorterBarcodeScannerActivity)?.onBackPressed()
            }
            R.id.button_cancel->{
                dismiss()
            }
        }
    }

    interface DataListener {
        fun onDataReceived(data: Double?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DataListener) {
            dataListener = context
        } else {
            throw ClassCastException("$context must implement DataListener")
        }
    }


    private fun getTimeAsDoubleFromText(timeText: String): Double? {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date: Date? = try {
            format.parse(timeText)
        } catch (e: Exception) {
            null
        }

        if (date != null) {
            val calendar = Calendar.getInstance().apply { time = date }
            val hours = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            val totalTimeInMinutes = hours * 60 + minutes
            return totalTimeInMinutes/60.toDouble()
        }

        return null
    }
    private fun isValidTimeFormat(input: String): Boolean {
        val pattern = Regex("^\\d{2}:\\d{2}$")
        return input.matches(pattern)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }




}