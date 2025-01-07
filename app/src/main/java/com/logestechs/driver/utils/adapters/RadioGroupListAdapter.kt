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
    private var sortedList: List<Map.Entry<String, String>> = reorderList(list)
    var selectedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioGroupListViewHolder {
        mContext = parent.context

        val inflater =
            ItemRadioGroupListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RadioGroupListViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: RadioGroupListViewHolder, position: Int) {
        val item = sortedList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = sortedList.size

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
            sortedList[selectedPosition!!].key
        } else {
            null
        }
    }

    fun selectItem(position: Int) {
        selectedPosition = position
        notifyItemChanged(position)
    }

    private fun reorderList(originalList: LinkedHashMap<String, String>?): List<Map.Entry<String, String>> {
        if (originalList == null) return emptyList()
        val otherReasonEntry = originalList.entries.find { it.key == "OTHER_REASON" }
        val filteredEntries = originalList.entries.filter { it.key != "OTHER_REASON" }
        return if (otherReasonEntry != null) {
            filteredEntries + otherReasonEntry
        } else {
            filteredEntries
        }
    }

    fun updateList(newList: LinkedHashMap<String, String>?) {
        list = newList
        sortedList = reorderList(newList)
        notifyDataSetChanged()
    }

    class RadioGroupListViewHolder(
        private val binding: ItemRadioGroupListBinding,
        private var parent: ViewGroup,
        private var mAdapter: RadioGroupListAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Map.Entry<String, String>) {
            binding.radioButton.text = item.value
            binding.radioButton.isChecked = adapterPosition == mAdapter.selectedPosition

            binding.radioButton.setOnClickListener {
                mAdapter.applySelection(adapterPosition)
                mAdapter.listener?.onItemSelected(item.value)
            }
        }
    }
}