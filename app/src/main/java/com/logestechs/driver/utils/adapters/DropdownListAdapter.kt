package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DropdownItem
import com.logestechs.driver.utils.DropdownTag
import com.logestechs.driver.utils.interfaces.OnDropDownItemClickListener

class DropdownListAdapter(
    var list: List<DropdownItem>,
    private val mListener: OnDropDownItemClickListener,
    var tag: DropdownTag
) :
    RecyclerView.Adapter<DropdownItemViewHolder>() {

    lateinit var mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DropdownItemViewHolder {
        mContext = parent.context
        val inflater = LayoutInflater.from(parent.context)
        return DropdownItemViewHolder(inflater, parent, this, mListener)
    }

    override fun onBindViewHolder(holder: DropdownItemViewHolder, position: Int) {
        val item: DropdownItem = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    fun update(itemsList: List<DropdownItem>) {
        list = itemsList
        this.notifyDataSetChanged()
    }
}

class DropdownItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    private var mAdapter: DropdownListAdapter,
    private var mListener: OnDropDownItemClickListener
) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_dropdown, parent, false)) {
    private var mTitleTextView: TextView? = null
    private var mSecondaryTitleTextView: TextView? = null

    init {
        mTitleTextView = itemView.findViewById(R.id.text_title)
        mSecondaryTitleTextView = itemView.findViewById(R.id.text_secondary_title)
    }

    fun bind(dropdownItem: DropdownItem) {
        mTitleTextView?.text = dropdownItem.toString()
        itemView.setOnClickListener {
            mListener.onItemClick(mAdapter.list[adapterPosition], mAdapter.tag)
        }
    }

}
