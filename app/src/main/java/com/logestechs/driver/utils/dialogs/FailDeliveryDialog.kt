package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.BuildConfig
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.FailDeliveryRequestBody
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogFailDeliveryBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.RadioGroupListAdapter
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.interfaces.FailDeliveryDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener
import java.io.File
import java.io.IOException

class FailDeliveryDialog(
    var context: Context,
    var listener: FailDeliveryDialogListener?,
    var pkg: Package?,
    var loadedImagesList: ArrayList<LoadedImage>
) : RadioGroupListListener, ThumbnailsListListener {

    lateinit var binding: DialogFailDeliveryBinding
    lateinit var alertDialog: AlertDialog

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private val loginResponse = SharedPreferenceWrapper.getLoginResponse()

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogFailDeliveryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_fail_delivery, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        if (loginResponse?.user?.companyID == 368.toLong()) {
            binding.etReason.visibility = android.view.View.GONE
        }

        binding.rvThumbnails.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ThumbnailsAdapter(loadedImagesList, this@FailDeliveryDialog)
        }

        binding.buttonDone.setOnClickListener {
            if (validateInput()) {
                alertDialog.dismiss()
                listener?.onFailDelivery(
                    FailDeliveryRequestBody(
                        binding.etReason.text.toString(),
                        (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                        getPodImagesUrls(),
                        pkg?.id
                    )
                )
            }
        }

        binding.buttonCaptureImage.setOnClickListener {
            listener?.onCaptureImage()
        }

        binding.buttonLoadImage.setOnClickListener {
            listener?.onLoadImage()
        }

        binding.rvReasons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RadioGroupListAdapter(
                SharedPreferenceWrapper.getDriverCompanySettings()?.failureReasons?.fail,
                this@FailDeliveryDialog
            )
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
            return  false
        }

        if (companyConfigurations?.isForceDriversToAddAttachments == true) {
            if (loadedImagesList.isEmpty()){
                Helper.showErrorMessage(
                    context,
                    getStringForFragment(R.string.error_add_attachments)
                )
                return  false
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
