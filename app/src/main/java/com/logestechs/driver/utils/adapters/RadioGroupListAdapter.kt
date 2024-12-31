package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.databinding.ItemRadioGroupListBinding
import com.logestechs.driver.utils.interfaces.RadioGroupListListener


class RadioGroupListAdapter(
    var list: LinkedHashMap<String, String>?,
    var listener: RadioGroupListListener?
) :
    RecyclerView.Adapter<RadioGroupListAdapter.RadioGroupListViewHolder>() {

    private lateinit var mContext: Context
    var selectedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioGroupListViewHolder {
        mContext = parent.context

        val inflater =
            ItemRadioGroupListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RadioGroupListViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: RadioGroupListViewHolder, position: Int) {
        val item: MutableMap.MutableEntry<String, String>? = list?.entries?.elementAt(position)
        holder.bind(item)
    }

    override fun getItemCount(): Int = list?.size ?: 0

    fun applySelection(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        if (previousPosition != null) {
            notifyItemChanged(previousPosition)
        }
        if (selectedPosition != null) {
            notifyItemChanged(selectedPosition!!)
        }
    }

    fun getSelectedItem(): String? {
        return if (selectedPosition != null) {
            list?.entries?.elementAt(selectedPosition!!)?.key
        } else {
            null
        }
    }

    fun selectItem(position: Int) {
        selectedPosition = position
        notifyItemChanged(position)
    }

    class RadioGroupListViewHolder(
        private val binding: ItemRadioGroupListBinding,
        private var parent: ViewGroup,
        private var mAdapter: RadioGroupListAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MutableMap.MutableEntry<String, String>?) {
            binding.radioButton.text = item?.value
            binding.radioButton.isChecked = adapterPosition == mAdapter.selectedPosition

            binding.radioButton.setOnClickListener {
                mAdapter.applySelection(adapterPosition)
                mAdapter.listener?.onItemSelected(item?.value)
            }

        }
    }
}