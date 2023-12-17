package com.logestechs.driver.utils.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.RejectItemRequestBody
import com.logestechs.driver.data.model.ItemDetails
import com.logestechs.driver.databinding.ItemScannedProductItemBinding
import com.logestechs.driver.utils.dialogs.RejectItemDialog
import com.logestechs.driver.utils.interfaces.RejectItemDialogListener
import com.logestechs.driver.utils.interfaces.ScannedShippingPlanItemCardListener
import com.logestechs.driver.data.model.ProductItem


class ScannedShippingPlanItemCellAdapter(
    var list: ArrayList<ItemDetails?>,
    var listener: ScannedShippingPlanItemCardListener?,
    var rejectItemDialogListener: RejectItemDialogListener?,
    var productItem: ProductItem?
) :
    RecyclerView.Adapter<ScannedShippingPlanItemCellAdapter.ScannedShippingPlanItemViewHolder>(),
    RejectItemDialogListener{

    var context: Context? = null
    var isRejectedItems: Boolean? = null
    private fun removeItemByBarcode(barcode: String?) {
        val position = list.indexOfFirst { it?.barcode == barcode }
        if (position >= 0) {
            deleteItem(position)
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScannedShippingPlanItemViewHolder {
        context = parent.context

        val inflater =
            ItemScannedProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ScannedShippingPlanItemViewHolder(inflater, parent, this) // Pass the rejectItemDialogListener and pkg
    }

    override fun onBindViewHolder(holder: ScannedShippingPlanItemViewHolder, position: Int) {
        val itemDetails: ItemDetails? = list[position]
        holder.bind(itemDetails)

    }

    override fun getItemCount(): Int = list.size

    fun deleteItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clearList() {
        val size = list.size
        list.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun insertItem(items: List<ItemDetails>?, rejectedItems: Boolean? = null) {
        if (rejectedItems == true) {
            isRejectedItems = true
        }
        if (!items.isNullOrEmpty()) {
            list.addAll(0, items)
            notifyItemRangeChanged(0, items.size)
            notifyItemRangeInserted(0, items.size)
        }
    }


    fun getItem(index: Int?): ItemDetails? {
        return list[index!!]
    }

    override fun onItemRejected(rejectItemRequestBody: RejectItemRequestBody) {
        val barcode = rejectItemRequestBody.barcode
        removeItemByBarcode(barcode)
        listener?.rejectItem(rejectItemRequestBody)
    }

    class ScannedShippingPlanItemViewHolder(
        private val binding: ItemScannedProductItemBinding,
        private var parent: ViewGroup,
        private var mAdapter: ScannedShippingPlanItemCellAdapter,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemDetails: ItemDetails?) {
            if (mAdapter.isRejectedItems == true) {
                binding.buttonContextMenu.visibility = View.GONE
            }
            binding.itemBarcode.textItem.text = itemDetails?.barcode
            binding.itemProductName.textItem.text = itemDetails?.name
            binding.itemProductSku.textItem.text = itemDetails?.sku
            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.scanned_shipping_plan_item_context_menu)
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    if (mAdapter.context != null) {
                        when (item?.itemId) {
                            R.id.action_reject_item -> {
                                RejectItemDialog(
                                    mAdapter.context!!,
                                    mAdapter,
                                    itemDetails?.barcode
                                ).showDialog()
                            }
                        }
                    }
                    true
                }
                popup.show()
            }
        }
    }
}
