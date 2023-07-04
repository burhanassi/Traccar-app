package com.logestechs.driver.ui.barcodeScanner

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.logestechs.driver.R
import com.logestechs.driver.databinding.FragmentSetTimeSpentBinding
import com.logestechs.driver.ui.dashboard.FulfilmentSorterDashboardActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SetTimeSpent : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentSetTimeSpentBinding
    private var hours: Double? = null
    private var dataListener: DataListener? = null

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
        binding.buttonDoneFragment.setOnClickListener(this)
    }
    override fun onStart() {
        super.onStart()

        val width = resources.getDimensionPixelSize(R.dimen.dialog_width)
        val height = resources.getDimensionPixelSize(R.dimen.dialog_height)

        dialog?.window?.setLayout(width, height)
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_done_fragment -> {
                hours = getTimeAsDoubleFromText(binding.editTextNumberHours.text.toString())
                dataListener?.onDataReceived(hours)
                dismiss()
                (activity as? FulfilmentSorterBarcodeScannerActivity)?.onBackPressed()
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
    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dismiss()
    }



}