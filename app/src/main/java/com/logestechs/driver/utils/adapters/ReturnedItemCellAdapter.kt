package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.requests.RejectItemRequestBody
import com.logestechs.driver.data.model.ItemDetails
import com.logestechs.driver.data.model.LoadedImage
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.databinding.ItemScannedProductItemBinding
import com.logestechs.driver.databinding.ReturnedItemScannedProductItemBinding
import com.logestechs.driver.utils.dialogs.RejectItemDialog
import com.logestechs.driver.utils.interfaces.RejectItemDialogListener
import com.logestechs.driver.utils.interfaces.ScannedShippingPlanItemCardListener


class ReturnedItemCellAdapter(
    var list: ArrayList<ProductItem?>,
    var listener: ScannedShippingPlanItemCardListener?,
    var loadedImagesList: ArrayList<LoadedImage>
) :
    RecyclerView.Adapter<ReturnedItemCellAdapter.ReturnedItemViewHolder>() {

    var context: Context? = null
    var isRejectedItems: Boolean? = null
    fun removeItemByBarcode(barcode: String?) {
        val position = list.indexOfFirst { it?.barcode == barcode }
        if (position >= 0) {
            deleteItem(position)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReturnedItemViewHolder {
        context = parent.context

        val inflater =
            ReturnedItemScannedProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ReturnedItemViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: ReturnedItemViewHolder, position: Int) {
        val productItem: ProductItem? = list[position]
        holder.bind(productItem)

    }

    override fun getItemCount(): Int = list.size

    fun deleteItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    fun clearList() {
        val size = list.size
        list.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun insertItem(items: List<ProductItem>?, rejectedItems: Boolean? = null) {
        if (rejectedItems == true) {
            isRejectedItems = true
        }
        if (!items.isNullOrEmpty()) {
            list.addAll(0, items)
            notifyItemRangeChanged(0, items.size)
            notifyItemRangeInserted(0, items.size)
        }
    }

    fun getItem(index: Int?): ProductItem? {
        return list[index!!]
    }

    class ReturnedItemViewHolder(
        private val binding: ReturnedItemScannedProductItemBinding,
        private var parent: ViewGroup,
        private var mAdapter: ReturnedItemCellAdapter,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(productItem: ProductItem?) {
            if (mAdapter.isRejectedItems == true) {
                binding.buttonContextMenu.visibility = View.GONE
            }
            binding.itemBarcode.textItem.text = productItem?.barcode
            binding.itemProductBarcode.textItem.text = productItem?.productBarcode
            binding.itemProductName.textItem.text = productItem?.name
            binding.itemProductSku.textItem.text = productItem?.sku
            if (productItem?.previousLocationBarcode != null && productItem.previousLocationBarcode!!.isNotEmpty()) {
                binding.itemBinLocation.textItem.text = productItem.previousLocationBarcode
                if (productItem.previousBinBarcode != null && productItem.previousBinBarcode!!.isNotEmpty()) {
                    binding.itemBinLocation.textItem.text =
                        "${productItem.previousLocationBarcode} - ${productItem.previousBinBarcode}"
                }
            }
            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.scanned_shipping_plan_item_context_menu)
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    if (mAdapter.context != null) {
                        when (item?.itemId) {
                            R.id.action_reject_item -> {
                                mAdapter.listener?.onShowRejectItemDialog(productItem?.barcode!!)
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
