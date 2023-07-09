package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.databinding.ItemPickedFulfilmentOrderBinding
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.interfaces.PickedFulfilmentOrderCardListener


class PickedFulfilmentOrderCellAdapter(
    var fulfilmentOrdersList: ArrayList<FulfilmentOrder?>,
    var context: Context?,
    var listener: PickedFulfilmentOrderCardListener?,
) :
    RecyclerView.Adapter<PickedFulfilmentOrderCellAdapter.PickedFulfilmentOrderViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): PickedFulfilmentOrderViewHolder {
        val inflater =
            ItemPickedFulfilmentOrderBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return PickedFulfilmentOrderViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        fulfilmentOrderViewHolder: PickedFulfilmentOrderViewHolder,
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

    class PickedFulfilmentOrderViewHolder(
        private var binding: ItemPickedFulfilmentOrderBinding,
        private var parent: ViewGroup,
        private var mAdapter: PickedFulfilmentOrderCellAdapter
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

            binding.textItemsCount.text = fulfilmentOrder?.numberOfItems?.toString()
            binding.textSkuCount.text = fulfilmentOrder?.items?.size.toString()

            binding.buttonPack.setOnClickListener {
                mAdapter.listener?.onPackFulfilmentOrder(adapterPosition)
            }

            binding.itemOrderBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, fulfilmentOrder?.barcode)
            }
        }
    }
}