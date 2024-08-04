package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.github.chrisbanes.photoview.PhotoView
import com.logestechs.driver.R
import com.logestechs.driver.databinding.DialogShowAttachmentsBinding
import com.logestechs.driver.databinding.DialogShowPackageContentBinding
import com.smarteist.autoimageslider.SliderViewAdapter

class ShowPackageContentDialog(
    var context: Context?,
    var description: String?
){
    lateinit var binding: DialogShowPackageContentBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogShowPackageContentBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_show_package_content,
            null,
            false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.description.text = description
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}
