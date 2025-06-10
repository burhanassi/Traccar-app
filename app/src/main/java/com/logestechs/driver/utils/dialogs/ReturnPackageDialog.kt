package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.ReturnPackageRequestBody
import com.logestechs.driver.api.responses.GetDriverCompanySettingsResponse
import com.logestechs.driver.data.model.CompanyInfo
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogReturnPackageBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.RadioGroupListAdapter
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.interfaces.RadioGroupListListener
import com.logestechs.driver.utils.interfaces.ReturnPackageDialogListener
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ReturnPackageDialog(
    var context: Context?,
    var listener: ReturnPackageDialogListener?,
    var pkg: Package?,
    var loadedImagesList: ArrayList<LoadedImage>
) : RadioGroupListListener , ThumbnailsListListener{

    lateinit var binding: DialogReturnPackageBinding
    lateinit var alertDialog: AlertDialog
    private var companyInfo: GetDriverCompanySettingsResponse? = SharedPreferenceWrapper.getDriverCompanySettings()
    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogReturnPackageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_return_package, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        if (companyInfo?.driverCompanyConfigurations?.id == 397.toLong()) {
            binding.switchReceiverPaidCostsContainer.visibility = View.GONE
        } else {
            binding.switchReceiverPaidCostsContainer.visibility = View.VISIBLE
        }
        if (pkg?.isReceiverPayCost == true) {
            binding.switchReceiverPaidCosts.isChecked = true
            if (pkg?.partnerPackageId != null) {
                callGetFirstPartnerCost(null)
            }
        }

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            if (validateInput()) {
                alertDialog.dismiss()
                listener?.onPackageReturned(
                    ReturnPackageRequestBody(
                        binding.etReason.text.toString(),
                        (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                        binding.switchReceiverPaidCosts.isChecked,
                        getPodImagesUrls(),
                        pkg
                    )
                )
            }
        }

        binding.rvReasons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RadioGroupListAdapter(
                SharedPreferenceWrapper.getDriverCompanySettings()?.failureReasons?.returnShipment,
                this@ReturnPackageDialog
            )
        }

        binding.rvThumbnails.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ThumbnailsAdapter(loadedImagesList, this@ReturnPackageDialog)
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

        binding.switchReceiverPaidCosts.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && pkg?.partnerPackageId != null && pkg?.partnerPackageId?.toInt() != 0) {
                callGetFirstPartnerCost(
                    ReturnPackageRequestBody(
                        null,
                        (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                        true,
                        null,
                        null
                    )
                )
            } else {
                binding.containerFirstPartnerCost.visibility = View.GONE
            }
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun callGetFirstPartnerCost(body: ReturnPackageRequestBody?) {
        (context as? LogesTechsActivity)?.showWaitDialog()
        if (Helper.isInternetAvailable(context)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response =
                        ApiAdapter.apiClient.getFirstPartnerCost(pkg?.id, body)
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            binding.containerFirstPartnerCost.visibility = View.VISIBLE
                            binding.firstPartnerCost.text = response.body()?.cost.toString()
                        }
                    } else {
                        try {
                            val jObjError = JSONObject(response?.errorBody()!!.string())
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    context,
                                    jObjError.optString(AppConstants.ERROR_KEY)
                                )
                            }

                        } catch (e: java.lang.Exception) {
                            withContext(Dispatchers.Main) {
                                Helper.showErrorMessage(
                                    context,
                                    context?.getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(context, context?.getString(R.string.cannot_open_attachments))
                        } else {
                            Helper.showErrorMessage(context, e.stackTraceToString())
                        }
                    }
                } finally {
                    (context as? LogesTechsActivity)?.hideWaitDialog()
                }
            }
        } else {
            (context as? LogesTechsActivity)?.hideWaitDialog()
            Helper.showErrorMessage(
                this@ReturnPackageDialog.context, this@ReturnPackageDialog.context?.getString(R.string.error_check_internet_connection)
            )
        }
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
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etReason.clearFocus()
    }

    override fun onItemSelected(title: String?) {
        val selectedReasonKey = (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem()
        binding.etReason.setText(title)
        clearFocus()
        if (binding.switchReceiverPaidCosts.isChecked && pkg?.partnerPackageId != null && pkg?.partnerPackageId?.toInt() != 0) {
            callGetFirstPartnerCost(
                ReturnPackageRequestBody(
                    null,
                    selectedReasonKey,
                    true,
                    null,
                    null
                )
            )
        }
    }

    override fun onDeleteImage(position: Int) {
        listener?.onDeleteImage(position)
    }
}
