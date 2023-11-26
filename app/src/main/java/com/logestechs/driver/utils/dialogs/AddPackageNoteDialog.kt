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
import com.logestechs.driver.api.requests.AddNoteRequestBody
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.DialogAddPackageNoteBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsApp
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.interfaces.AddPackageNoteDialogListener
import com.logestechs.driver.utils.interfaces.RadioGroupListListener
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener

class AddPackageNoteDialog(
    var context: Context,
    var listener: AddPackageNoteDialogListener?,
    var pkg: Package?,
    var loadedImagesList: ArrayList<LoadedImage>
) : RadioGroupListListener, ThumbnailsListListener {

    lateinit var binding: DialogAddPackageNoteBinding
    lateinit var alertDialog: AlertDialog

    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogAddPackageNoteBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_add_package_note, null, false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding

        binding.rvThumbnails.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = ThumbnailsAdapter(loadedImagesList, this@AddPackageNoteDialog)
        }

        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        binding.buttonDone.setOnClickListener {
            if (binding.etReason.text.toString().isNotEmpty()) {
                alertDialog.dismiss()
                listener?.onPackageNoteAdded(
                    AddNoteRequestBody(
                        binding.etReason.text.toString(),
                        getPodImagesUrls(),
                        pkg?.id
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
            listener?.onCaptureImage()
        }

        binding.buttonLoadImage.setOnClickListener {
            listener?.onLoadImage()
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
        val imm =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        binding.etReason.clearFocus()
    }

    override fun onItemSelected(title: String?) {
        binding.etReason.setText(title)
        clearFocus()
    }

    override fun onDeleteImage(position: Int) {
        listener?.onDeleteImage(position)
    }
}
