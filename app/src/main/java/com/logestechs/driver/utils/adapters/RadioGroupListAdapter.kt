package com.logestechs.driver.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.databinding.ItemRadioGroupListBinding
import com.logestechs.driver.utils.interfaces.RadioGroupListListener

class RadioGroupListAdapter(
    private var list: LinkedHashMap<String, String>?,
    private val listener: RadioGroupListListener?,
    private val showOtherOption: Boolean = false,
    private val reasonOther: String = ""
) : RecyclerView.Adapter<RadioGroupListAdapter.RadioGroupListViewHolder>() {

    private var selectedPosition: Int? = null

    // Filter out the "Other" option if not needed
    private val filteredItems: List<MutableMap.MutableEntry<String, String>> = list?.entries?.let {
        if (showOtherOption) it.toList() else it.filter { entry -> entry.value != reasonOther }
    } ?: emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioGroupListViewHolder {
        val binding = ItemRadioGroupListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RadioGroupListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RadioGroupListViewHolder, position: Int) {
        // Handle "Other" option separately if needed
        if (position == filteredItems.size) {
            holder.bindOther()
        } else {
            val item = filteredItems[position]
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = filteredItems.size + if (showOtherOption) 1 else 0

    // Apply selection and notify any changes in the selection
    fun applySelection(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        if (previousPosition != null) notifyItemChanged(previousPosition)
        if (selectedPosition != null) notifyItemChanged(selectedPosition!!)
    }

    fun getSelectedItem(): String? {
        return if (selectedPosition != null && selectedPosition!! < filteredItems.size) {
            filteredItems[selectedPosition!!].key
        } else {
            null
        }
    }

    // Select item at a given position
    fun selectItem(position: Int) {
        applySelection(position)
    }

    inner class RadioGroupListViewHolder(
        private val binding: ItemRadioGroupListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind regular items (not the "Other" option)
        fun bind(item: MutableMap.MutableEntry<String, String>?) {
            binding.radioButton.text = item?.value
            binding.radioButton.isChecked = adapterPosition == selectedPosition

            binding.radioButton.setOnClickListener {
                applySelection(adapterPosition)
                listener?.onItemSelected(item?.value)
            }
        }

        // Bind the "Other" option
        fun bindOther() {
            binding.radioButton.text = itemView.context.getString(R.string.reason_other)
            binding.radioButton.isChecked = adapterPosition == selectedPosition

            binding.radioButton.setOnClickListener {
                applySelection(adapterPosition)
                listener?.onItemSelected(itemView.context.getString(R.string.reason_other))
            }
        }
    }
}
