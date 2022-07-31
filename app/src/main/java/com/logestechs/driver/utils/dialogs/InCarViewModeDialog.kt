package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogInCarViewModeBinding
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.interfaces.InCarViewModeDialogListener

class InCarViewModeDialog(
    var context: Context,
    var delegate: InCarViewModeDialogListener
) {

    lateinit var binding: DialogInCarViewModeBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogInCarViewModeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_in_car_view_mode, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()


//        binding.buttonCancel.setOnClickListener {
//            alertDialog.dismiss()
//        }

//        binding.buttonDone.setOnClickListener {
//            if (binding.etNotes.text.toString().isNotEmpty()) {
//                delegate.addNote(pkg, binding.etNotes.text.toString())
//                alertDialog.dismiss()
//            } else {
//                Helper.showErrorMessage(
//                    context,
//                    getStringForFragment(R.string.please_insert_note)
//                )
//            }
//        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}

