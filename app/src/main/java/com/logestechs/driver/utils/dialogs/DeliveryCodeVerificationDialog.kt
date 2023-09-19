package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.Bundles
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogDeliveryCodeVerificationBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.interfaces.VerificationCodeDialogListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class DeliveryCodeVerificationDialog(
    var context: Context,
    var listener: VerificationCodeDialogListener?,
    var pkg: Package? = null,
    var isBundle: Boolean? = false,
    var bundles: Bundles? = null
) {

    lateinit var binding: DialogDeliveryCodeVerificationBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogDeliveryCodeVerificationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_delivery_code_verification, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.buttonDone.setOnClickListener {
            clearFocus()
            if (binding.etVerificationCode.getText().isEmpty()) {
                Helper.showErrorMessage(
                    context, getString(
                        R.string.error_insert_verification_code,
                    )
                )
            } else if (binding.etVerificationCode.getText().length != 4) {
                Helper.showErrorMessage(
                    context, getString(
                        R.string.error_verification_code_must_be_four_digits,
                    )
                )
            } else {
                callVerifyDeliveryPin(pkg?.id, binding.etVerificationCode.getText())
            }
        }

        binding.root.setOnClickListener {
            clearFocus()
        }

        binding.imageViewResend.setOnClickListener {
            clearFocus()
            listener?.onResendPinSms()
        }

        this.alertDialog = alertDialog
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun callVerifyDeliveryPin(packageId: Long?, pinCode: String?) {
        showWaitDialog()
        if (Helper.isInternetAvailable(context)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = if (isBundle == true) {
                        ApiAdapter.apiClient.verifyDeliveryPinForBundles(bundles?.id, pinCode)
                    } else {
                        ApiAdapter.apiClient.verifyDeliveryPin(packageId, pinCode)
                    }

                    withContext(Dispatchers.Main) {
                        hideWaitDialog()
                    }
                    if (response?.isSuccessful == true && response.body() != null) {
                        withContext(Dispatchers.Main) {
                            listener?.onPackageVerified()
                            alertDialog.dismiss()
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
                                    getString(R.string.error_general)
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    hideWaitDialog()
                    Helper.logException(e, Throwable().stackTraceToString())
                    withContext(Dispatchers.Main) {
                        if (e.message != null && e.message!!.isNotEmpty()) {
                            Helper.showErrorMessage(context, e.message)
                        } else {
                            Helper.showErrorMessage(context, e.stackTraceToString())
                        }
                    }
                }
            }
        } else {
            hideWaitDialog()
            Helper.showErrorMessage(
                context, getString(R.string.error_check_internet_connection)
            )
        }
    }


    private fun getString(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

    private fun clearFocus() {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etVerificationCode.editText.clearFocus()
    }


    fun showWaitDialog() {
        try {
            if ((context is LogesTechsActivity)) {
                (context as LogesTechsActivity).showWaitDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideWaitDialog() {
        try {
            if ((context is LogesTechsActivity)) {
                (context as LogesTechsActivity).hideWaitDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
