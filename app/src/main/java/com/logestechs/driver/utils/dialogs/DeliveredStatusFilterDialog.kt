package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogDeliveredStatusFilterBinding
import com.logestechs.driver.utils.DeliveredPackageStatus
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.customViews.StatusSelector
import com.logestechs.driver.utils.interfaces.DeliveredStatusFilterDialogListener

class DeliveredStatusFilterDialog (
    var context: Context,
    var listener: DeliveredStatusFilterDialogListener,
    private var selectedStatus: DeliveredPackageStatus
) {


    lateinit var binding: DialogDeliveredStatusFilterBinding
    lateinit var alertDialog: AlertDialog

    private val selectors: ArrayList<StatusSelector> = ArrayList()

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogDeliveredStatusFilterBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_delivered_status_filter, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        selectors.clear()
        selectors.addAll(
            arrayListOf(
                binding.selectorAll,
                binding.selectorDelivered,
                binding.selectorPartiallyDelivered
            )
        )
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.selectorAll.setOnClickListener {
            selectedStatus = DeliveredPackageStatus.ALL
            clearSelection()
            binding.selectorAll.makeSelected()
        }
        binding.selectorDelivered.setOnClickListener {
            selectedStatus = DeliveredPackageStatus.DELIVERED
            clearSelection()
            binding.selectorDelivered.makeSelected()
        }
        binding.selectorPartiallyDelivered.setOnClickListener {
            selectedStatus = DeliveredPackageStatus.PARTIALLY_DELIVERED
            clearSelection()
            binding.selectorPartiallyDelivered.makeSelected()
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
            DeliveredPackageStatus.ALL -> {
                binding.selectorAll.makeSelected()
            }
            DeliveredPackageStatus.DELIVERED -> {
                binding.selectorDelivered.makeSelected()
            }
            DeliveredPackageStatus.PARTIALLY_DELIVERED -> {
                binding.selectorPartiallyDelivered.makeSelected()
            }
            else -> {
                binding.selectorAll.makeSelected()
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