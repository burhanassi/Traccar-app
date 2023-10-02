package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.CheckIns
import com.logestechs.driver.databinding.ItemCheckInBinding
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager

class CheckInsCellAdapter(
    var checkInsList: ArrayList<CheckIns?>,
    var context: Context?
) : RecyclerView.Adapter<CheckInsCellAdapter.CheckInsViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): CheckInsCellAdapter.CheckInsViewHolder {
        val inflater =
            ItemCheckInBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return CheckInsCellAdapter.CheckInsViewHolder(
            inflater,
            viewGroup,
            this
        )
    }

    override fun onBindViewHolder(
        checkInsViewHolder: CheckInsCellAdapter.CheckInsViewHolder,
        position: Int
    ) {
        val checkIns: CheckIns? = checkInsList[position]
        checkInsViewHolder.bind(checkIns)
    }

    override fun getItemCount(): Int {
        return checkInsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<CheckIns>?) {
        this.checkInsList.clear()
        this.checkInsList.addAll(list!!)
        this.notifyDataSetChanged()
    }

    fun deleteItem(index: Int) {
        checkInsList.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, checkInsList.size)
    }

    class CheckInsViewHolder(
        var binding: ItemCheckInBinding,
        private var parent: ViewGroup,
        private var mAdapter: CheckInsCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(checkIns: CheckIns?) {
            if (mAdapter.context != null) {
            }
            binding.itemCheckInDate.textItem.text =
                Helper.formatServerDate(checkIns?.timestamp, DateFormats.DEFAULT_FORMAT)
            binding.itemCheckIns.textItem.text = checkIns?.vehicleName
            binding.itemWarehouse.textItem.text = checkIns?.hub?.name
        }
    }
}