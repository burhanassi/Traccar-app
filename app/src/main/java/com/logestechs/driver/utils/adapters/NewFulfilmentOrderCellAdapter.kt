package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.databinding.ItemNewFulfilmentOrderBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.interfaces.NewFulfilmentOrderCardListener

class NewFulfilmentOrderCellAdapter(
    var fulfilmentOrdersList: ArrayList<FulfilmentOrder?>,
    var context: Context?,
    var listener: NewFulfilmentOrderCardListener?,
) :
    RecyclerView.Adapter<NewFulfilmentOrderCellAdapter.NewFulfilmentOrderViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): NewFulfilmentOrderViewHolder {
        val inflater =
            ItemNewFulfilmentOrderBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return NewFulfilmentOrderViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        fulfilmentOrderViewHolder: NewFulfilmentOrderViewHolder,
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

    class NewFulfilmentOrderViewHolder(
        private var binding: ItemNewFulfilmentOrderBinding,
        private var parent: ViewGroup,
        private var mAdapter: NewFulfilmentOrderCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fulfilmentOrder: FulfilmentOrder?) {
            binding.itemOrderBarcode.textItem.text = fulfilmentOrder?.barcode
            binding.itemCustomerAddress.textItem.text =
                fulfilmentOrder?.receiverAddress?.toStringAddress()
            binding.itemCustomerName.textItem.text = fulfilmentOrder?.customerName

            if (fulfilmentOrder?.notes?.trim().isNullOrEmpty()) {
                binding.itemNotes.root.visibility = View.GONE
            } else {
                binding.itemNotes.root.visibility = View.VISIBLE
                binding.itemNotes.textItem.text = fulfilmentOrder?.notes
            }

            binding.textItemsCount.text = fulfilmentOrder?.items?.size.toString()
            binding.textSkuCount.text = fulfilmentOrder?.items?.size.toString()

            binding.buttonPick.setOnClickListener {
                mAdapter.listener?.onPickFulfilmentOrder(adapterPosition)
            }

            binding.itemOrderBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, fulfilmentOrder?.barcode)
            }
        }
    }
}