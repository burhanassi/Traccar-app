package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.databinding.ItemPickedFulfilmentOrderBinding
import com.logestechs.driver.utils.FulfilmentOrderStatus
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
        return PickedFulfilmentOrderViewHolder(inflater, viewGroup, this, viewGroup.context)
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
        private var mAdapter: PickedFulfilmentOrderCellAdapter,
        private var context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fulfilmentOrder: FulfilmentOrder?) {
            val isPicked = fulfilmentOrder?.status == FulfilmentOrderStatus.PICKED.name
            binding.itemOrderBarcode.textItem.text = fulfilmentOrder?.barcode
            binding.itemCustomerAddress.textItem.text =
                fulfilmentOrder?.receiverAddress?.toStringAddress()
            binding.itemCustomerName.textItem.text = fulfilmentOrder?.customerName
            if (isPicked) {
                binding.buttonPack.text = context.getString(R.string.button_pack)
            } else {
                binding.buttonPack.text = context.getString(R.string.button_continue_picking)
            }
            if (fulfilmentOrder?.notes?.trim().isNullOrEmpty()) {
                binding.itemNotes.root.visibility = View.GONE
            } else {
                binding.itemNotes.root.visibility = View.VISIBLE
                binding.itemNotes.textItem.text = fulfilmentOrder?.notes
            }

            binding.textItemsCount.text = fulfilmentOrder?.numberOfItems?.toString()
            binding.textSkuCount.text = fulfilmentOrder?.items?.size.toString()

//            binding.buttonPack.setOnClickListener {
//                if(isPicked){
//                    mAdapter.listener?.onPackFulfilmentOrder(adapterPosition)
//                }else{
//                    mAdapter.listener?.onContinuePickingClicked(adapterPosition)
//                }
//            }
            binding.buttonPack.setOnClickListener {
                if (mAdapter.listener != null) {
                    if (isPicked) {
                        mAdapter.listener!!.onPackFulfilmentOrder(adapterPosition)
                    } else {
                        mAdapter.listener!!.onContinuePickingClicked(fulfilmentOrder)
                    }
                }
            }

            binding.itemOrderBarcode.buttonCopy.setOnClickListener {
                Helper.copyTextToClipboard(mAdapter.context, fulfilmentOrder?.barcode)
            }
        }
    }
}