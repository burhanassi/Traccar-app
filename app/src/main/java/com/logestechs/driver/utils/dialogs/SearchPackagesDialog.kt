package com.logestechs.driver.utils.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogSearchPackagesBinding
import com.logestechs.driver.ui.singleScanBarcodeScanner.SingleScanBarcodeScanner
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.interfaces.SearchPackagesDialogListener

class SearchPackagesDialog(
    var context: Context,
    var listener: SearchPackagesDialogListener
) {

    lateinit var binding: DialogSearchPackagesBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogSearchPackagesBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_search_packages, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonPickup.setOnClickListener {
            if (binding.etSearchWord.getText().isNotEmpty()) {
                alertDialog.dismiss()
                listener.onPackageSearch(binding.etSearchWord.getText())
            } else {
                binding.etSearchWord.makeInvalid()
            }
        }

        binding.buttonScan.setOnClickListener {
            val mIntent = Intent(context, SingleScanBarcodeScanner::class.java)
            (context as Activity).startActivity(mIntent)
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }
}