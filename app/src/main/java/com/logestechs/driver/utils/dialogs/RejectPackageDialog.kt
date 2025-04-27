package com.logestechs.driver.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.RejectPackageRequestBody
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.databinding.DialogRejectPackageBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.interfaces.ImageActionListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener
import com.logestechs.driver.utils.interfaces.RejectPackageDialogListener
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener

class RejectPackageDialog(
    private val context: Context,
    private val listener: RejectPackageDialogListener?,
    private val imageActionListener: ImageActionListener?,
    var loadedImagesList: ArrayList<LoadedImage>,
): ThumbnailsListListener {

    lateinit var binding: DialogRejectPackageBinding
    private lateinit var alertDialog: AlertDialog


    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogRejectPackageBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_reject_package,
            null,
            false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.rvThumbnails.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ThumbnailsAdapter(loadedImagesList, this@RejectPackageDialog)
        }

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            if (binding.etReason.text.toString().isNotEmpty()) {
                alertDialog.dismiss()
                listener?.onPackageRejected(
                    RejectPackageRequestBody(
                        binding.etReason.text.toString(),
                        getPodImagesUrls()
                    )
                )
            } else {
                Helper.showErrorMessage(
                    context,
                    getStringForFragment(R.string.error_insert_message_text)
                )
            }
        }

        binding.buttonCaptureImage.setOnClickListener {
            imageActionListener?.onCaptureImage()
        }

        binding.buttonLoadImage.setOnClickListener {
            imageActionListener?.onLoadImage()
        }

        binding.root.setOnClickListener {
            clearFocus()
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun getPodImagesUrls(): List<String?>? {
        return if (loadedImagesList.isNotEmpty()) {
            val list: ArrayList<String?> = ArrayList()
            for (item in loadedImagesList) {
                list.add(item.imageUrl)
            }
            list
        } else {
            null
        }
    }


    private fun getStringForFragment(resId: Int): String {
        return LogesTechsApp.instance.resources.getString(resId)
    }

    private fun clearFocus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etReason.clearFocus()
    }

    override fun onDeleteImage(position: Int) {
        imageActionListener?.onDeleteImage(position)
    }
}
