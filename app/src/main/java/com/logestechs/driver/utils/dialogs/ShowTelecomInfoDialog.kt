package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.ChangePackageTypeRequestBody
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogChangePackageTypeBinding
import com.logestechs.driver.databinding.DialogShowTelecomInfoBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.adapters.RadioGroupListAdapter
import com.logestechs.driver.utils.interfaces.ChangePackageTypeDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener

class ShowTelecomInfoDialog(
    var context: Context,
    var pkg: Package?
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

        if (pkg?.invoiceNumber != null) {
            binding.etSupplierInvoice.text = pkg?.invoiceNumber
        } else {
            binding.etSupplierInvoice.text = "N/A"
        }

        if (pkg?.thirdPartyTrackingNo != null) {
            binding.etThirdPartyTrackingNo.text = pkg?.thirdPartyTrackingNo.toString()
        } else {
            binding.etThirdPartyTrackingNo.text = "N/A"
        }

        if (pkg?.isFingerprintRequired != null) {
            binding.etIsFingerprintRequired.text = if (pkg?.isFingerprintRequired == true) { "YES"} else { "NO"}
        }

        if (pkg?.thirdPartyBarcode != null) {
            binding.etThirdPartyBarcode.text = pkg?.thirdPartyBarcode
        } else {
            binding.etThirdPartyBarcode.text = "N/A"
        }

        if (pkg?.accountReferenceNumber != null) {
            binding.etAccountReferenceNumber.text = pkg?.accountReferenceNumber
        } else {
            binding.etAccountReferenceNumber.text = "N/A"
        }

        if (pkg?.msisdn != null) {
            binding.etMsisdn.text = pkg?.msisdn
        } else {
            binding.etMsisdn.text = "N/A"
        }

        if (pkg?.simNumber != null) {
            binding.etSimNumber.text = pkg?.simNumber
        } else {
            binding.etSimNumber.text = "N/A"
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}
