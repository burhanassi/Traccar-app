package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.PostponePackageRequestBody
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogPostponePackageBinding
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.RadioGroupListAdapter
import com.logestechs.driver.utils.interfaces.PostponePackageDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class PostponePackageDialog(
    var context: Context,
    var listener: PostponePackageDialogListener?,
    var pkg: Package?
) : RadioGroupListListener {

    lateinit var binding: DialogPostponePackageBinding
    lateinit var alertDialog: AlertDialog
    private val myCalendar: Calendar = Calendar.getInstance()

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogPostponePackageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_postpone_package, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            if (binding.etReason.text.toString().isNotEmpty()) {
                if (binding.textDate.text.toString().isNotEmpty()) {
                    alertDialog.dismiss()
                    listener?.onPackagePostponed(
                        PostponePackageRequestBody(
                            binding.etReason.text.toString(),
                            (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                            binding.textDate.text.toString(),
                            pkg?.id
                        )
                    )
                } else {
                    Helper.showErrorMessage(
                        context,
                        getStringForFragment(R.string.error_select_postpone_date)
                    )
                    Helper.changeImageStrokeColor(
                        binding.imageViewCalendar,
                        R.color.red_flamingo,
                        context
                    )
                }

            } else {
                Helper.showErrorMessage(
                    context,
                    getStringForFragment(R.string.error_insert_message_text)
                )
            }

        }

        binding.containerDatePicker.setOnClickListener {
            val date =
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    myCalendar[Calendar.YEAR] = year
                    myCalendar[Calendar.MONTH] = monthOfYear
                    myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        binding.textDate.text =
                            myCalendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                .format(DateTimeFormatter.ofPattern(DateFormats.DEFAULT_FORMAT.value))
                        Helper.changeImageStrokeColor(
                            binding.imageViewCalendar,
                            R.color.fontTrackHint,
                            context
                        )
                    }
                }

            val datePickerDialog = DatePickerDialog(
                context, date, myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH],
            )

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.rvReasons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RadioGroupListAdapter(
                SharedPreferenceWrapper.getDriverCompanySettings()?.failureReasons?.postpone,
                this@PostponePackageDialog
            )
        }

        binding.root.setOnClickListener {
            clearFocus()
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

    private fun clearFocus() {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etReason.clearFocus()
    }

    override fun onItemSelected(title: String?) {
        binding.etReason.setText(title)
        clearFocus()
    }
}
