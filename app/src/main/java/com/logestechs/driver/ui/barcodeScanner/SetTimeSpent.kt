package com.logestechs.driver.ui.barcodeScanner

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.logestechs.driver.R
import com.logestechs.driver.databinding.FragmentSetTimeSpentBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SetTimeSpent : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentSetTimeSpentBinding
    lateinit var alertDialog: AlertDialog
    private var hours: Double? = null
    private var dataListener: DataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetTimeSpentBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)
        binding.timePicker.setIs24HourView(true)
        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, _ ->
            if (hourOfDay > 8) {
                binding.timePicker.hour = 8
            }
        }
        binding.buttonDone.setOnClickListener(this)
        binding.buttonCancel.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        val dialogWidth = resources.displayMetrics.widthPixels * 0.9f
        val dialogHeight = resources.displayMetrics.heightPixels * 0.65f

        dialog?.window?.setLayout(dialogWidth.toInt(), dialogHeight.toInt())

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_done -> {
                hours = getTimeAsDoubleFromTimePicker(binding.timePicker)
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getTimeAsDoubleFromTimePicker(timePicker: TimePicker): Double? {
        val hours = timePicker.hour
        val minutes = timePicker.minute
        val totalTimeInMinutes = hours * 60 + minutes
        return totalTimeInMinutes/60.toDouble()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }
}
