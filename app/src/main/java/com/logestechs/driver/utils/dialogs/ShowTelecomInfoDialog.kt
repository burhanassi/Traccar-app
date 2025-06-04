package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.api.responses.GetTelecomInfoResponse
import com.logestechs.driver.databinding.DialogShowTelecomInfoBinding
import retrofit2.Response

class ShowTelecomInfoDialog(
    var context: Context,
    var response: GetTelecomInfoResponse?
) {

    lateinit var binding: DialogShowTelecomInfoBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogShowTelecomInfoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_show_telecom_info, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        if (response?.supplierInvoice != null) {
            binding.etSupplierInvoice.text = response?.supplierInvoice
        } else {
            binding.etSupplierInvoice.text = "N/A"
        }

        if (response?.thirdPartyTrackingNo != null) {
            binding.etThirdPartyTrackingNo.text = response?.thirdPartyTrackingNo.toString()
        } else {
            binding.etThirdPartyTrackingNo.text = "N/A"
        }

        if (response?.isFingerprintRequired != null) {
            binding.etIsFingerprintRequired.text = if (response?.isFingerprintRequired == true) { "YES"} else { "NO"}
        }

        if (response?.thirdPartyBarcode != null) {
            binding.etThirdPartyBarcode.text = response?.thirdPartyBarcode
        } else {
            binding.etThirdPartyBarcode.text = "N/A"
        }

        if (response?.accountReferenceNumber != null) {
            binding.etAccountReferenceNumber.text = response?.accountReferenceNumber
        } else {
            binding.etAccountReferenceNumber.text = "N/A"
        }

        if (response?.msisdn != null) {
            binding.etMsisdn.text = response?.msisdn
        } else {
            binding.etMsisdn.text = "N/A"
        }

        if (response?.simNumber != null) {
            binding.etSimNumber.text = response?.simNumber
        } else {
            binding.etSimNumber.text = "N/A"
        }

        if (response?.accountManagerName != null) {
            binding.etAccountManagerName.text = response?.accountManagerName
        } else {
            binding.etAccountManagerName.text = "N/A"
        }

        if (response?.accountManagerNumber != null) {
            binding.etAccountManagerNumber.text = response?.accountManagerNumber
        } else {
            binding.etAccountManagerNumber.text = "N/A"
        }

        if (response?.cr != null) {
            binding.etCr.text = response?.cr
        } else {
            binding.etCr.text = "N/A"
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}
