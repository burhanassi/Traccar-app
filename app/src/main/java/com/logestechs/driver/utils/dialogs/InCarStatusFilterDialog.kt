package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogInCarStatusFilterBinding
import com.logestechs.driver.utils.InCarPackageStatus
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.customViews.StatusSelector
import com.logestechs.driver.utils.interfaces.InCarStatusFilterDialogListener

class InCarStatusFilterDialog(
    var context: Context,
    var listener: InCarStatusFilterDialogListener,
    var selectedStatus: InCarPackageStatus
) {

    lateinit var binding: DialogInCarStatusFilterBinding
    lateinit var alertDialog: AlertDialog

    private val selectors: ArrayList<StatusSelector> = ArrayList()

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogInCarStatusFilterBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_in_car_status_filter, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        selectors.clear()
        selectors.addAll(
            arrayListOf(
                binding.selectorToDeliver,
                binding.selectorAll,
                binding.selectorFailed,
                binding.selectorCod,
                binding.selectorPostponed,
                binding.selectorPickup,
                binding.selectorDelivery
            )
        )
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.selectorToDeliver.setOnClickListener {
            selectedStatus = InCarPackageStatus.TO_DELIVER
            clearSelection()
            binding.selectorToDeliver.makeSelected()
        }
        binding.selectorPickup.setOnClickListener {
            selectedStatus = InCarPackageStatus.TO_DELIVER_PICKUP
            clearSelection()
            binding.selectorPickup.makeSelected()
        }
        binding.selectorDelivery.setOnClickListener {
            selectedStatus = InCarPackageStatus.TO_DELIVER_DELIVERY
            clearSelection()
            binding.selectorDelivery.makeSelected()
        }
        binding.selectorAll.setOnClickListener {
            selectedStatus = InCarPackageStatus.ALL
            clearSelection()
            binding.selectorAll.makeSelected()
        }
        binding.selectorFailed.setOnClickListener {
            selectedStatus = InCarPackageStatus.FAILED
            clearSelection()
            binding.selectorFailed.makeSelected()
        }
        binding.selectorCod.setOnClickListener {
            selectedStatus = InCarPackageStatus.COD
            clearSelection()
            binding.selectorCod.makeSelected()
        }
        binding.selectorPostponed.setOnClickListener {
            selectedStatus = InCarPackageStatus.POSTPONED
            clearSelection()
            binding.selectorPostponed.makeSelected()
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
            InCarPackageStatus.TO_DELIVER -> {
                binding.selectorToDeliver.makeSelected()
            }
            InCarPackageStatus.ALL -> {
                binding.selectorAll.makeSelected()
            }
            InCarPackageStatus.FAILED -> {
                binding.selectorFailed.makeSelected()
            }
            InCarPackageStatus.POSTPONED -> {
                binding.selectorPostponed.makeSelected()
            }
            InCarPackageStatus.COD -> {
                binding.selectorCod.makeSelected()
            }
            InCarPackageStatus.TO_DELIVER_PICKUP -> {
                binding.selectorPickup.makeSelected()
            }
            InCarPackageStatus.TO_DELIVER_DELIVERY -> {
                binding.selectorDelivery.makeSelected()
            }
            else -> {
                binding.selectorToDeliver.makeSelected()
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

