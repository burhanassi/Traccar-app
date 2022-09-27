package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.FailDeliveryRequestBody
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogFailDeliveryBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.adapters.RadioGroupListAdapter
import com.logestechs.driver.utils.interfaces.FailDeliveryDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener

class FailDeliveryDialog(
    var context: Context,
    var listener: FailDeliveryDialogListener?,
    var pkg: Package?
) : RadioGroupListListener {

    lateinit var binding: DialogFailDeliveryBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogFailDeliveryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_fail_delivery, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            if (binding.etReason.text.toString().isNotEmpty()) {
                alertDialog.dismiss()
                listener?.onFailDelivery(
                    FailDeliveryRequestBody(
                        binding.etReason.text.toString(),
                        (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                        pkg?.id
                    )
                )
            } else {
                Helper.showErrorMessage(
                    context,
                    getStringForFragment(R.string.error_insert_message_text)
                )
            }

        }

        binding.rvReasons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RadioGroupListAdapter(
                SharedPreferenceWrapper.getDriverCompanySettings()?.failureReasons?.fail,
                this@FailDeliveryDialog
            )
        }

        binding.root.setOnClickListener {
            clearFocus()
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

    private fun clearFocus() {
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etReason.clearFocus()
    }

    override fun onItemSelected(title: String?) {
        binding.etReason.setText(title)
        clearFocus()
    }
}
