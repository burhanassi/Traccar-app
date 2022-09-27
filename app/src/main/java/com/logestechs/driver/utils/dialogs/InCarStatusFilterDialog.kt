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
import com.logestechs.driver.utils.interfaces.InCarStatusFilterDialogListener

class InCarStatusFilterDialog(
    var context: Context,
    var listener: InCarStatusFilterDialogListener,
    var selectedStatus: InCarPackageStatus
) {

    lateinit var binding: DialogInCarStatusFilterBinding
    lateinit var alertDialog: AlertDialog

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
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.selectorToDeliver.setOnClickListener {
            selectedStatus = InCarPackageStatus.TO_DELIVER
            binding.selectorToDeliver.makeSelected()
            binding.selectorAll.makeUnselected()
            binding.selectorFailed.makeUnselected()
            binding.selectorCod.makeUnselected()
            binding.selectorPostponed.makeUnselected()
        }
        binding.selectorAll.setOnClickListener {
            selectedStatus = InCarPackageStatus.ALL
            binding.selectorToDeliver.makeUnselected()
            binding.selectorAll.makeSelected()
            binding.selectorFailed.makeUnselected()
            binding.selectorCod.makeUnselected()
            binding.selectorPostponed.makeUnselected()
        }
        binding.selectorFailed.setOnClickListener {
            selectedStatus = InCarPackageStatus.FAILED
            binding.selectorToDeliver.makeUnselected()
            binding.selectorAll.makeUnselected()
            binding.selectorFailed.makeSelected()
            binding.selectorCod.makeUnselected()
            binding.selectorPostponed.makeUnselected()
        }
        binding.selectorCod.setOnClickListener {
            selectedStatus = InCarPackageStatus.COD
            binding.selectorToDeliver.makeUnselected()
            binding.selectorAll.makeUnselected()
            binding.selectorFailed.makeUnselected()
            binding.selectorCod.makeSelected()
            binding.selectorPostponed.makeUnselected()
        }
        binding.selectorPostponed.setOnClickListener {
            selectedStatus = InCarPackageStatus.POSTPONED
            binding.selectorToDeliver.makeUnselected()
            binding.selectorAll.makeUnselected()
            binding.selectorFailed.makeUnselected()
            binding.selectorCod.makeUnselected()
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
            else -> {
                binding.selectorToDeliver.makeSelected()
            }
        }
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}

