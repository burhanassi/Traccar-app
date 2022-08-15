package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.ChangePackageTypeRequestBody
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogChangePackageTypeBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.PackageType
import com.logestechs.driver.utils.adapters.RadioGroupListAdapter
import com.logestechs.driver.utils.interfaces.ChangePackageTypeDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener

class ChangePackageTypeDialog(
    var context: Context,
    var listener: ChangePackageTypeDialogListener?,
    var pkg: Package?
) : RadioGroupListListener {

    lateinit var binding: DialogChangePackageTypeBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogChangePackageTypeBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_change_package_type, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            alertDialog.dismiss()
            listener?.onPackageTypeChanged(
                ChangePackageTypeRequestBody(
                    (binding.rvReasons.adapter as RadioGroupListAdapter).getSelectedItem(),
                    pkg?.id
                )
            )
        }

        binding.rvReasons.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RadioGroupListAdapter(
                Helper.getPackageTypes(),
                this@ChangePackageTypeDialog
            )
        }

        when (pkg?.shipmentType) {
            PackageType.COD.name -> {
                (binding.rvReasons.adapter as RadioGroupListAdapter).selectItem(0)
            }
            PackageType.REGULAR.name -> {
                (binding.rvReasons.adapter as RadioGroupListAdapter).selectItem(1)
            }
            PackageType.SWAP.name -> {
                (binding.rvReasons.adapter as RadioGroupListAdapter).selectItem(2)
            }
            PackageType.BRING.name -> {
                (binding.rvReasons.adapter as RadioGroupListAdapter).selectItem(3)
            }
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

    override fun onItemSelected(title: String?) {
    }
}
