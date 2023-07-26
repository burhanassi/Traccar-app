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
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.api.requests.DeleteImageRequestBody
import com.logestechs.driver.api.responses.PackageAttachmentsResponseBody
import com.logestechs.driver.databinding.DialogShowAttachmentsBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsFragment
import com.logestechs.driver.utils.adapters.ThumbnailsAdapter
import com.logestechs.driver.utils.interfaces.ShowAttachmentsDialogListener
import com.smarteist.autoimageslider.SliderViewAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ShowAttachmentsDialog(
    var context: Context?,
    var listener: ShowAttachmentsDialogListener?,
    var packageId: Long?,
    var packageAttachmentsResponseBody: PackageAttachmentsResponseBody?
){
    lateinit var binding: DialogShowAttachmentsBinding
    lateinit var alertDialog: AlertDialog


    fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(context, 0)
        val binding: DialogShowAttachmentsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_show_attachments,
            null,
            false
        )
        dialogBuilder.setView(binding.root)
        val alertDialog = dialogBuilder.create()
        this.binding = binding
        binding.buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
        listener?.showAttachments(packageId)
        setupImageSlider()
    }

    private fun setupImageSlider() {
        val imageUrls = packageAttachmentsResponseBody?.imageUrls
        val adapter = imageUrls?.let { SliderAdapter(it) }
        binding.sliderView.setSliderAdapter(adapter!!)
    }

    inner class SliderAdapter(private val imageResources: List<String>) :
        SliderViewAdapter<SliderAdapter.SliderViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup): SliderViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_image_slider, parent, false)
            return SliderViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
            Glide.with(context!!)
                .load(imageResources[position])
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.photoView)
        }

        override fun getCount(): Int {
            return imageResources.size
        }

        inner class SliderViewHolder(itemView: View) : SliderViewAdapter.ViewHolder(itemView) {
            val photoView: PhotoView = itemView.findViewById(R.id.photoView)
        }
    }
}
