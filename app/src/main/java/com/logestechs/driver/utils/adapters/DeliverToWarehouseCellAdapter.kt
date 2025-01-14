package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.databinding.ItemPackedFulfilmentOrderBinding
import com.logestechs.driver.utils.AppLanguages
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.FulfillmentItemStatus
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.interfaces.DeliverToWarehouseCardListener
import com.yariksoffice.lingver.Lingver

class DeliverToWarehouseCellAdapter(
    private var fulfilmentOrdersList: ArrayList<FulfilmentOrder?>,
    var context: Context?,
    var listener: DeliverToWarehouseCardListener?
) : RecyclerView.Adapter<DeliverToWarehouseCellAdapter.DeliverToWarehouseViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): DeliverToWarehouseViewHolder {
        val inflater =
            ItemPackedFulfilmentOrderBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return DeliverToWarehouseViewHolder(inflater, this)
    }

    override fun onBindViewHolder(
        fulfilmentOrderViewHolder: DeliverToWarehouseViewHolder,
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

    class DeliverToWarehouseViewHolder(
        private var binding: ItemPackedFulfilmentOrderBinding,
        private var mAdapter: DeliverToWarehouseCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fulfilmentOrder: FulfilmentOrder?) {
            binding.itemOrderBarcode.textItem.text = fulfilmentOrder?.barcode
            binding.itemPackageBarcode.textItem.text = fulfilmentOrder?.packageBarcode
            binding.itemCustomerName.textItem.text = fulfilmentOrder?.customerName
            if (fulfilmentOrder?.createdDate.isNullOrEmpty()) {
                binding.containerPackedDate.visibility = View.GONE
            } else {
                binding.containerPackedDate.visibility = View.VISIBLE
                binding.itemPackedDate.textItem.text = Helper.formatServerDate(fulfilmentOrder?.createdDate, DateFormats.DEFAULT_FORMAT)
            }
            binding.textItemsCount.text = fulfilmentOrder?.numberOfItems.toString()

            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                binding.textStatus.text = FulfillmentItemStatus.PACKED.arabic
            } else {
                binding.textStatus.text = FulfillmentItemStatus.PACKED.english
            }

            binding.buttonDeliverToWarehouse.setOnClickListener {
                mAdapter.listener?.onDeliverToWarehouse(adapterPosition)
            }
        }
    }
}