package com.logestechs.driver.utils.dialogs

import android.annotation.SuppressLint
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

        binding.buttonDone.setOnClickListener {
            if (validateInput()) {
                alertDialog.dismiss()
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            listener?.onPackagePostponed(
                                PostponePackageRequestBody(
                                    binding.etReason.text.toString(),
                                    (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                                    location.longitude,
                                    location.latitude,
                                    binding.textDate.text.toString(),
                                    getPodImagesUrls(),
                                    pkg?.id
                                )
                            )
                        } else {
                            listener?.onPackagePostponed(
                                PostponePackageRequestBody(
                                    binding.etReason.text.toString(),
                                    (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                                    null,
                                    null,
                                    binding.textDate.text.toString(),
                                    getPodImagesUrls(),
                                    pkg?.id
                                )
                            )
                        }
                    }
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

    override fun onItemSelected(title: String?) {
        binding.etReason.setText(title)
        clearFocus()
    }

    override fun onDeleteImage(position: Int) {
        listener?.onDeleteImage(position)
    }
}
