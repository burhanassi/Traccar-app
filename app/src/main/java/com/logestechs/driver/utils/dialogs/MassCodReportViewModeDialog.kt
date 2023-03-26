package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogMassCodReportsViewModeBinding
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.MassCodReportsViewMode
import com.logestechs.driver.utils.interfaces.MassCodReportViewModeDialogListener

class MassCodReportViewModeDialog(
    var context: Context,
    var delegate: MassCodReportViewModeDialogListener,
    var selectedViewMode: MassCodReportsViewMode
) {

    lateinit var binding: DialogMassCodReportsViewModeBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogMassCodReportsViewModeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_mass_cod_reports_view_mode, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.selectorCustomers.setOnClickListener {
            selectedViewMode = MassCodReportsViewMode.BY_CUSTOMER
            binding.selectorCustomers.makeSelected()
            binding.selectorMassCodReports.makeUnselected()
        }

        binding.selectorMassCodReports.setOnClickListener {
            selectedViewMode = MassCodReportsViewMode.BY_REPORT
            binding.selectorMassCodReports.makeSelected()
            binding.selectorCustomers.makeUnselected()
        }

        binding.buttonDone.setOnClickListener {
            alertDialog.dismiss()
            delegate.onViewModeChanged(selectedViewMode)
        }

        handleSelection()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun handleSelection() {
        when (selectedViewMode) {
            MassCodReportsViewMode.BY_CUSTOMER -> {
                binding.selectorCustomers.makeSelected()
            }
            MassCodReportsViewMode.BY_REPORT -> {
                binding.selectorMassCodReports.makeSelected()
            }
        }
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}

