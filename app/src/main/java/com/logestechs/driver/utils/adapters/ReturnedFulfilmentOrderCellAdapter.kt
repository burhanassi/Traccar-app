package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.databinding.ItemReturnedFulfilmentOrderBinding
import com.logestechs.driver.utils.AppLanguages
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.interfaces.ReturnedFulfilmentOrderCardListener
import com.yariksoffice.lingver.Lingver

class ReturnedFulfilmentOrderCellAdapter(
    private var fulfilmentOrdersList: ArrayList<FulfilmentOrder?>,
    var context: Context?,
    var listener: ReturnedFulfilmentOrderCardListener?
) : RecyclerView.Adapter<ReturnedFulfilmentOrderCellAdapter.ReturnedFulfilmentOrderViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ReturnedFulfilmentOrderViewHolder {
        val inflater =
            ItemReturnedFulfilmentOrderBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return ReturnedFulfilmentOrderViewHolder(inflater, this)
    }

    override fun onBindViewHolder(
        fulfilmentOrderViewHolder: ReturnedFulfilmentOrderViewHolder,
        position: Int
    ) {
        val fulfilmentOrder: FulfilmentOrder? = fulfilmentOrdersList[position]
        fulfilmentOrderViewHolder.bind(fulfilmentOrder)
    }

    override fun getItemCount(): Int {
        return fulfilmentOrdersList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    fun clearList() {
        val size: Int = fulfilmentOrdersList.size
        fulfilmentOrdersList.clear()
        notifyItemRangeRemoved(0, size)
    }

    class ReturnedFulfilmentOrderViewHolder(
        private var binding: ItemReturnedFulfilmentOrderBinding,
        private var mAdapter: ReturnedFulfilmentOrderCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fulfilmentOrder: FulfilmentOrder?) {
            binding.itemOrderBarcode.textItem.text = fulfilmentOrder?.barcode
            binding.itemPackageBarcode.textItem.text = fulfilmentOrder?.packageBarcode
            binding.itemCustomerName.textItem.text = fulfilmentOrder?.customerName
            if (fulfilmentOrder?.returnDate.isNullOrEmpty()) {
                binding.containerReturnDate.visibility = View.GONE
            } else {
                binding.containerReturnDate.visibility = View.VISIBLE
                binding.itemReturnedDate.textItem.text = Helper.formatServerDate(fulfilmentOrder?.returnDate, DateFormats.DEFAULT_FORMAT)
            }
            binding.textItemsCount.text = fulfilmentOrder?.numberOfItems.toString()

            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                binding.textStatus.text = fulfilmentOrder?.getStatusInArabic()
            } else {
                binding.textStatus.text = fulfilmentOrder?.getStatusInEnglish()
            }

            binding.buttonReturn.setOnClickListener {
                mAdapter.listener?.onReturnFulfilmentOrder(adapterPosition)
            }
        }
    }
}