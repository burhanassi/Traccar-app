package com.logestechs.driver.ui.barcodeScanner

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
    lateinit var alertDialog: AlertDialog
    private var hours: Double? = null
    private var dataListener: DataListener? = null

    private val hoursOptions = arrayOf("00", "01", "02", "03", "04","05", "06", "07", "08")
    private val minOptions = arrayOf("00","01", "02", "03", "04", "05","06", "07", "08", "09"
        ,"10", "11", "12", "13", "14","15", "16", "17", "18","19"
        ,"20", "21", "22", "23", "24","25", "26", "27", "28","29"
        ,"30", "31", "32", "33", "34","35", "36", "37", "38","39"
        ,"40", "41", "42", "43", "44","45", "46", "47", "48","49"
        ,"50", "51", "52", "53", "54","55", "56", "57", "58","59")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)


        val adapterHours = CenteredArrayAdapter<String>(requireContext(), R.layout.spinner_item_centered, hoursOptions)
        adapterHours.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeHours.adapter = adapterHours
        val adapterMin = CenteredArrayAdapter<String>(requireContext(), R.layout.spinner_item_centered, minOptions)
        adapterMin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeMin.adapter = adapterMin

        binding.buttonDone.setOnClickListener(this)
        binding.buttonCancel.setOnClickListener(this)
    }
    override fun onStart() {
        super.onStart()

        val dialogWidth = resources.displayMetrics.widthPixels * 0.9f
        val dialogHeight = resources.displayMetrics.heightPixels * 0.33f

        dialog?.window?.setLayout(dialogWidth.toInt(), dialogHeight.toInt())

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_done -> {
                hours = getTimeAsDoubleFromText(binding.spinnerTimeHours.selectedItem.toString()+":"+binding.spinnerTimeMin.selectedItem.toString())
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
    override fun onDestroy() {
        super.onDestroy()
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }
}