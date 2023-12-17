package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogInCarViewModeBinding
import com.logestechs.driver.utils.InCarPackagesViewMode
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.interfaces.InCarViewModeDialogListener

class InCarViewModeDialog(
    var context: Context,
    var delegate: InCarViewModeDialogListener,
    var selectedViewMode: InCarPackagesViewMode,
    var isSprint: Boolean = false
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
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.selectorVillages.setOnClickListener {
            selectedViewMode = InCarPackagesViewMode.BY_VILLAGE
            binding.selectorVillages.makeSelected()
            binding.selectorReceivers.makeUnselected()
            binding.selectorVendors.makeUnselected()
            binding.selectorPackages.makeUnselected()
        }

        binding.selectorPackages.setOnClickListener {
            selectedViewMode = InCarPackagesViewMode.PACKAGES
            binding.selectorVillages.makeUnselected()
            binding.selectorReceivers.makeUnselected()
            binding.selectorVendors.makeUnselected()
            binding.selectorPackages.makeSelected()
        }

        binding.selectorVendors.setOnClickListener {
            selectedViewMode = InCarPackagesViewMode.BY_CUSTOMER
            binding.selectorVillages.makeUnselected()
            binding.selectorReceivers.makeUnselected()
            binding.selectorVendors.makeSelected()
            binding.selectorPackages.makeUnselected()
        }

        binding.selectorReceivers.setOnClickListener {
            selectedViewMode = InCarPackagesViewMode.BY_RECEIVER
            binding.selectorVillages.makeUnselected()
            binding.selectorReceivers.makeSelected()
            binding.selectorVendors.makeUnselected()
            binding.selectorPackages.makeUnselected()
        }


        binding.buttonDone.setOnClickListener {
            alertDialog.dismiss()
            delegate.onViewModeChanged(selectedViewMode)
        }

        if (isSprint) {
            binding.selectorPackages.textView.text =
                getStringForFragment(R.string.view_mode_packages_sprint)
        }
        handleSelection()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun handleSelection() {
        when (selectedViewMode) {
            InCarPackagesViewMode.PACKAGES -> {
                binding.selectorPackages.makeSelected()
            }
            InCarPackagesViewMode.BY_VILLAGE -> {
                binding.selectorVillages.makeSelected()
            }
            InCarPackagesViewMode.BY_CUSTOMER -> {
                binding.selectorVendors.makeSelected()
            }
            InCarPackagesViewMode.BY_RECEIVER -> {
                binding.selectorReceivers.makeSelected()
            }
        }
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}

