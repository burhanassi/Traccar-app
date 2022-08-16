package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.CodChangeRequestBody
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogEditPackageCodBinding
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.interfaces.ChangeCodDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener


class ChangeCodDialog(
    var context: Context,
    var listener: ChangeCodDialogListener?,
    var pkg: Package?
) : RadioGroupListListener {

    lateinit var binding: DialogEditPackageCodBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogEditPackageCodBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_edit_package_cod, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.textCodAmount.text = pkg?.cod?.format()
        binding.itemPackageBarcode.textItem.text = pkg?.barcode
        binding.itemReceiverPhone.textItem.text = pkg?.receiverPhone
        binding.itemReceiverName.textItem.text = pkg?.getFullReceiverName()
        binding.itemCreatedDate.textItem.text =
            Helper.formatServerDate(pkg?.createdDate, DateFormats.DEFAULT_FORMAT)
        binding.itemReceiverAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()
        if (pkg?.notes.isNullOrEmpty()) {
            binding.itemNotes.root.visibility = View.GONE
        } else {
            binding.itemNotes.root.visibility = View.VISIBLE
            binding.itemNotes.textItem.text = pkg?.notes
        }

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            if (binding.etCodValue.editText.text.toString().isNotEmpty()) {
                alertDialog.dismiss()
                binding.etCodValue.makeValid()
                listener?.onCodChanged(
                    CodChangeRequestBody(
                        pkg?.id,
                        binding.etCodValue.editText.text.toString().toDouble()
                    )
                )
            } else {
                binding.etCodValue.makeInvalid()
            }
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

    override fun onItemSelected(title: String?) {
    }
}
