package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.databinding.ItemThumbnailBinding
import com.logestechs.driver.utils.interfaces.ThumbnailsListListener

class ThumbnailsAdapter(
    var list: ArrayList<LoadedImage>,
    val listener: ThumbnailsListListener?
) :
    RecyclerView.Adapter<ThumbnailsAdapter.ThumbnailViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        mContext = parent.context

        val inflater =
            ItemThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThumbnailViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }

    fun deleteItem(position: Int) {
        notifyItemRemoved(position)
    }

    class ThumbnailViewHolder(
        private val binding: ItemThumbnailBinding,
        private var parent: ViewGroup,
        private var mAdapter: ThumbnailsAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LoadedImage?) {
            binding.image.setImageURI(item?.imageUri)

            binding.buttonDeleteImage.setOnClickListener {
                mAdapter.listener?.onDeleteImage(adapterPosition)
            }
        }
    }
}
