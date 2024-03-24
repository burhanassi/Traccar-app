package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogChangeLocationsBinding
import com.logestechs.driver.utils.interfaces.ChangeLocationDialogListener

class ChangeLocationsDialog(
    var context: Context?,
    var listener: ChangeLocationDialogListener
) {
    lateinit var binding: DialogChangeLocationsBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogChangeLocationsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_change_locations,
            null,
            false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonChangeBin.setOnClickListener {
            alertDialog.dismiss()
            listener.onSelect(0)
        }
        binding.buttonChangeItem.setOnClickListener {
            alertDialog.dismiss()
            listener.onSelect(1)
        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}