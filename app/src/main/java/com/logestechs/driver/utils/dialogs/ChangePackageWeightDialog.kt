package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogChangePackageWeightBinding
import com.logestechs.driver.utils.interfaces.ChangePackageWeightDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener

class ChangePackageWeightDialog(
    var context: Context, var listener: ChangePackageWeightDialogListener?, var pkg: Package?
){

    lateinit var binding: DialogChangePackageWeightBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogChangePackageWeightBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_change_package_weight, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.etWeight.editText.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            val weightText = binding.etWeight.editText.text.toString()
            val weight = weightText.toFloatOrNull()

            if (weightText.isEmpty() || weight == null || weight == 0f) {
                binding.etWeight.makeInvalid()
            } else {
                binding.etWeight.makeValid()
                listener?.onPackageWeightChanged(pkg?.id)
                alertDialog.dismiss()
            }
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}
