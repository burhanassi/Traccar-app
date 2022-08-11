package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogReturnPackageBinding
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.interfaces.ReturnPackageDialogListener

class ReturnPackageDialog(
    var context: Context,
    var listener: ReturnPackageDialogListener?,
    var pkg: Package?
) {

    lateinit var binding: DialogReturnPackageBinding
    lateinit var alertDialog: AlertDialog

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
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            alertDialog.dismiss()
            listener?.onPackageReturned(pkg)
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}
