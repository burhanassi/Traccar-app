package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogReturnedStatusFilterBinding
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.ReturnedPackageStatus
import com.logestechs.driver.utils.customViews.StatusSelector
import com.logestechs.driver.utils.interfaces.ReturnedStatusFilterDialogListener

class ReturnedStatusFilterDialog (
    var context: Context,
    var listener: ReturnedStatusFilterDialogListener,
    var selectedStatus: ReturnedPackageStatus
){
    lateinit var binding: DialogReturnedStatusFilterBinding
    lateinit var alertDialog: AlertDialog

    private val selectors: ArrayList<StatusSelector> = ArrayList()

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogReturnedStatusFilterBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_returned_status_filter, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        selectors.clear()
        selectors.addAll(
            arrayListOf(
                binding.selectorAll,
                binding.selectorPartiallyDelivered,
                binding.selectorSwap,
                binding.selectorReturned
            )
        )
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.selectorAll.setOnClickListener {
            selectedStatus = ReturnedPackageStatus.ALL
            clearSelection()
            binding.selectorAll.makeSelected()
        }
        binding.selectorPartiallyDelivered.setOnClickListener {
            selectedStatus = ReturnedPackageStatus.PARTIALLY_DELIVERED
            clearSelection()
            binding.selectorPartiallyDelivered.makeSelected()
        }
        binding.selectorSwap.setOnClickListener {
            selectedStatus = ReturnedPackageStatus.SWAPPED
            clearSelection()
            binding.selectorSwap.makeSelected()
        }
        binding.selectorReturned.setOnClickListener {
            selectedStatus = ReturnedPackageStatus.RETURNED
            clearSelection()
            binding.selectorReturned.makeSelected()
        }

        binding.buttonDone.setOnClickListener {
            alertDialog.dismiss()
            listener.onStatusChanged(selectedStatus)
        }

        handleSelection()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun handleSelection() {
        when (selectedStatus) {
            ReturnedPackageStatus.ALL -> {
                binding.selectorAll.makeSelected()
            }
            ReturnedPackageStatus.PARTIALLY_DELIVERED -> {
                binding.selectorPartiallyDelivered.makeSelected()
            }
            ReturnedPackageStatus.SWAPPED -> {
                binding.selectorSwap.makeSelected()
            }
            ReturnedPackageStatus.RETURNED -> {
                binding.selectorReturned.makeSelected()
            }
        }
    }

    private fun clearSelection() {
        for (selector in selectors) {
            selector.makeUnselected()
        }
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}