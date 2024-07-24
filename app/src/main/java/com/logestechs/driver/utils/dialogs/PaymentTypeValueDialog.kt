package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogPaymentTypeValueBinding
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.customViews.StatusSelector
import com.logestechs.driver.utils.interfaces.PaymentTypeValueDialogListener
import kotlin.math.max

class PaymentTypeValueDialog(
    var context: Context,
    var listener: PaymentTypeValueDialogListener,
    private var selectedPaymentType: StatusSelector?,
    private var paymentTypeId: Long?
) {
    lateinit var binding: DialogPaymentTypeValueBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogPaymentTypeValueBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_payment_type_value, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.etValue.editText.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            val inputText = binding.etValue.getText()
            val inputDouble = inputText.toDoubleOrNull()

            if (inputText.isNotEmpty() && inputDouble != null && inputDouble != 0.0) {
                listener.onValueInserted(inputDouble, selectedPaymentType, paymentTypeId)
                alertDialog.dismiss()
            } else {
                binding.etValue.makeInvalid()
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
        binding.etValue.clearFocus()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}