package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogInsertBarcodeBinding
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.interfaces.InsertBarcodeDialogListener


class InsertBarcodeDialog(
    var context: Context,
    var listener: InsertBarcodeDialogListener
) {

    lateinit var binding: DialogInsertBarcodeBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogInsertBarcodeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_insert_barcode, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonPickup.setOnClickListener {
            alertDialog.dismiss()
            listener.onBarcodeInserted(binding.etBarcode.getText())
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}

