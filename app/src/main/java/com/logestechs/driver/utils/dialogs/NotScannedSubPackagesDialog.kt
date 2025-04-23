package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogNotSccannedSubpackagesBinding

class NotScannedSubPackagesDialog(
    var context: Context,
    var subpkg: MutableList<String>,
){

    lateinit var binding: DialogNotSccannedSubpackagesBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogNotSccannedSubpackagesBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_not_sccanned_subpackages, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        if (subpkg.isNotEmpty()){
            subpkg.forEach { item ->
                val textView = TextView(context).apply {
                    text = item
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 0)
                    }
                    gravity = Gravity.CENTER
                }
                binding.subpackagesContainer.addView(textView)
            }
        } else {
            val textView = TextView(context).apply {
                text = context.getString(R.string.all_subpackages_scanned)
                textSize = 18f
                setTextColor(ContextCompat.getColor(context, R.color.black))

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 0)
                }
                gravity = Gravity.CENTER
            }
            binding.subpackagesContainer.addView(textView)
        }

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}
