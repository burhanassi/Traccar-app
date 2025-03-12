package com.logestechs.driver.utils.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.PostponePackageRequestBody
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogPostponePackageBinding
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.RadioGroupListAdapter
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.interfaces.PostponePackageDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class PostponePackageDialog(
    var context: Context,
    var listener: PostponePackageDialogListener?,
    var pkg: Package?,
    var loadedImagesList: ArrayList<LoadedImage>
) : RadioGroupListListener, ThumbnailsListListener {

    lateinit var binding: DialogPostponePackageBinding
    lateinit var alertDialog: AlertDialog
    private val myCalendar: Calendar = Calendar.getInstance()

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations
    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()
    @SuppressLint("MissingPermission")
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

        binding.rvThumbnails.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ThumbnailsAdapter(loadedImagesList, this@PostponePackageDialog)
        }
        binding.etReason.visibility = android.view.View.GONE

        binding.buttonDone.setOnClickListener {
            if (validateInput()) {
                alertDialog.dismiss()
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            listener?.onPackagePostponed(
                                PostponePackageRequestBody(
                                    loginResponse?.user?.id,
                                    convertToIsoFormat(binding.textDate.text.toString()),
                                    binding.etReason.text.toString(),
                                    null,
                                    null,
                                    getPodImagesUrls(),
                                    pkg?.id
                                )
                            )
                        } else {
                            listener?.onPackagePostponed(
                                PostponePackageRequestBody(
                                    loginResponse?.user?.id,
                                    convertToIsoFormat(binding.textDate.text.toString()),
                                    binding.etReason.text.toString(),
                                    null,
                                    null,
                                    getPodImagesUrls(),
                                    pkg?.id
                                )
                            )
                        }
                    }
            }
        }

        binding.containerDatePicker.setOnClickListener {
            val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val time = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    myCalendar.set(Calendar.MINUTE, minute)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val formattedDateTime = myCalendar.time.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("${DateFormats.DEFAULT_FORMAT.value} HH:mm")) // Add time format
                        binding.textDate.text = formattedDateTime
                    } else {
                        val sdf = SimpleDateFormat("${DateFormats.DEFAULT_FORMAT.value} HH:mm", Locale.getDefault())
                        binding.textDate.text = sdf.format(myCalendar.time)
                    }
                    Helper.changeImageStrokeColor(
                        binding.imageViewCalendar,
                        R.color.fontTrackHint,
                        context
                    )
                }

                val timePickerDialog = TimePickerDialog(
                    context,
                    time,
                    myCalendar.get(Calendar.HOUR_OF_DAY),
                    myCalendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            }

            val datePickerDialog = DatePickerDialog(
                context, date, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
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

        binding.buttonCaptureImage.setOnClickListener {
            listener?.onCaptureImage()
        }

        binding.buttonLoadImage.setOnClickListener {
            listener?.onLoadImage()
        }

        binding.root.setOnClickListener {
            clearFocus()
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun validateInput(): Boolean {
        if ((binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem() == null) {
            Helper.showErrorMessage(
                context,
                getStringForFragment(R.string.title_please_select_reason)
            )
        }

        if (binding.etReason.text.isEmpty()) {
            Helper.showErrorMessage(
                context,
                getStringForFragment(R.string.error_insert_message_text)
            )
            return false
        }

        if (binding.textDate.text.toString().isEmpty()) {
            Helper.showErrorMessage(
                context,
                getStringForFragment(R.string.error_select_postpone_date)
            )
            Helper.changeImageStrokeColor(
                binding.imageViewCalendar,
                R.color.red_flamingo,
                context
            )
            return false
        } else {
            val selectedDateTimeString = binding.textDate.text.toString()
            val dateFormat = SimpleDateFormat("${DateFormats.DEFAULT_FORMAT.value} HH:mm", Locale.getDefault())
            val selectedDateTime = dateFormat.parse(selectedDateTimeString)
            val currentDateTime = Calendar.getInstance().time
            if (selectedDateTime != null && selectedDateTime.before(currentDateTime)) {
                Helper.showErrorMessage(
                    context,
                    getStringForFragment(R.string.error_date_must_be_in_future)
                )
                Helper.changeImageStrokeColor(
                    binding.imageViewCalendar,
                    R.color.red_flamingo,
                    context
                )
                return false
            }
        }

        if (companyConfigurations?.isForceDriversToAddAttachments == true) {
            if (loadedImagesList.isEmpty()) {
                Helper.showErrorMessage(
                    context,
                    getStringForFragment(R.string.error_add_attachments)
                )
                return false
            }

        }
        return true
    }

    private fun getPodImagesUrls(): List<String?>? {
        return if (loadedImagesList.isNotEmpty()) {
            val list: ArrayList<String?> = ArrayList()
            for (item in loadedImagesList) {
                list.add(item.imageUrl)
            }
            list
        } else {
            null
        }
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

    fun convertToIsoFormat(input: String): String {
        // Define the input format
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // Parse the input string
        val date = inputFormat.parse(input)

        // Define the output format
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("UTC") // Set timezone to UTC

        // Format the date into the desired output
        return outputFormat.format(date)
    }

    override fun onItemSelected(title: String?) {
        if (title == getStringForFragment(R.string.other)) {
            binding.etReason.visibility = android.view.View.VISIBLE
            binding.etReason.setText("")
            binding.etReason.requestFocus()
        } else {
            binding.etReason.visibility = android.view.View.GONE
            binding.etReason.setText(title)
        }
        clearFocus()
    }

    override fun onDeleteImage(position: Int) {
        listener?.onDeleteImage(position)
    }
}
