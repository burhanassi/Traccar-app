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
import com.logestechs.driver.api.requests.RejectItemRequestBody
import com.logestechs.driver.databinding.DialogRejectItemBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.adapters.RadioGroupListAdapter
import com.logestechs.driver.utils.interfaces.RejectItemDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener

class RejectItemDialog(
    private val context: Context,
    private val listener: RejectItemDialogListener?,
    private val barcode: String?

) : RadioGroupListListener {

    private lateinit var binding: DialogRejectItemBinding
    private lateinit var alertDialog: AlertDialog

    private val radioOptions = linkedMapOf(
        "DAMAGED" to context.getString(R.string.reason_damaged),
        "WRONG_COLOR" to context.getString(R.string.reason_color),
        "WRONG_ITEM" to context.getString(R.string.reason_item),
        "WRONG_SKU" to context.getString(R.string.reason_SKU),
        "OTHER" to context.getString(R.string.reason_other)
    )

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogRejectItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_reject_item,
            null,
            false
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
                listener?.onItemRejected(
                    RejectItemRequestBody(
                        barcode,
                        (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                        binding.etReason.text.toString()
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
            adapter = RadioGroupListAdapter(radioOptions, this@RejectItemDialog)
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
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etReason.clearFocus()
    }

    override fun onItemSelected(title: String?) {
        binding.etReason.setText(title)
        clearFocus()
    }
}
