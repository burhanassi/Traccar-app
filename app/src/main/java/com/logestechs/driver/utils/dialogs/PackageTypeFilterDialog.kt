package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogPackageTypeFilterBinding
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.interfaces.PackageTypeFilterDialogListener

class PackageTypeFilterDialog(
    var context: Context,
    var listener: PackageTypeFilterDialogListener,
    var selectedPackageType: PackageType
) {

    lateinit var binding: DialogPackageTypeFilterBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogPackageTypeFilterBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_package_type_filter, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.selectorRegular.setOnClickListener {
            selectedPackageType = PackageType.REGULAR
            binding.selectorRegular.makeSelected()
            binding.selectorAll.makeUnselected()
            binding.selectorSwap.makeUnselected()
            binding.selectorCod.makeUnselected()
            binding.selectorBring.makeUnselected()
        }
        binding.selectorAll.setOnClickListener {
            selectedPackageType = PackageType.ALL
            binding.selectorRegular.makeUnselected()
            binding.selectorAll.makeSelected()
            binding.selectorSwap.makeUnselected()
            binding.selectorCod.makeUnselected()
            binding.selectorBring.makeUnselected()
        }
        binding.selectorSwap.setOnClickListener {
            selectedPackageType = PackageType.SWAP
            binding.selectorRegular.makeUnselected()
            binding.selectorAll.makeUnselected()
            binding.selectorSwap.makeSelected()
            binding.selectorCod.makeUnselected()
            binding.selectorBring.makeUnselected()
        }
        binding.selectorCod.setOnClickListener {
            selectedPackageType = PackageType.COD
            binding.selectorRegular.makeUnselected()
            binding.selectorAll.makeUnselected()
            binding.selectorSwap.makeUnselected()
            binding.selectorCod.makeSelected()
            binding.selectorBring.makeUnselected()
        }
        binding.selectorBring.setOnClickListener {
            selectedPackageType = PackageType.BRING
            binding.selectorRegular.makeUnselected()
            binding.selectorAll.makeUnselected()
            binding.selectorSwap.makeUnselected()
            binding.selectorCod.makeUnselected()
            binding.selectorBring.makeSelected()
        }

        binding.buttonDone.setOnClickListener {
            alertDialog.dismiss()
            listener.onPackageTypeSelected(selectedPackageType)
        }

        handleSelection()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun handleSelection() {
        when (selectedPackageType) {
            PackageType.REGULAR -> {
                binding.selectorRegular.makeSelected()
            }
            PackageType.ALL -> {
                binding.selectorAll.makeSelected()
            }
            PackageType.SWAP -> {
                binding.selectorSwap.makeSelected()
            }
            PackageType.BRING -> {
                binding.selectorBring.makeSelected()
            }
            PackageType.COD -> {
                binding.selectorCod.makeSelected()
            }
        }
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}

