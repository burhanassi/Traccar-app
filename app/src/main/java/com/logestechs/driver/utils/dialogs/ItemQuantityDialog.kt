package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogItemQuantityBinding
import com.logestechs.driver.utils.interfaces.ItemQuantityDialogListener

class ItemQuantityDialog(
    var context: Context,
    var listener: ItemQuantityDialogListener,
    var barcode: String
) {
    lateinit var binding: DialogItemQuantityBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogItemQuantityBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_item_quantity, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
            listener.onDismiss()
        }

        binding.buttonDone.setOnClickListener {
            if (binding.etQuantity.getText().isNotEmpty()
                && binding.etQuantity.getText() != ""
                && binding.etQuantity.getText() != "0") {
                alertDialog.dismiss()
                listener.onQuantityInserted(binding.etQuantity.getText().toInt(), barcode)
            } else {
                binding.etQuantity.makeInvalid()
            }
        }

        binding.root.setOnClickListener {
            clearFocus()
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun clearFocus() {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etQuantity.clearFocus()
    }
}