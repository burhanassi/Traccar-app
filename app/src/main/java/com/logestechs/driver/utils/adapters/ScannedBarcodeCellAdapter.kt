package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.ScannedItem
import com.logestechs.driver.databinding.ItemScannedBarcodeBinding
import com.logestechs.driver.utils.BarcodeScanType
import com.logestechs.driver.utils.interfaces.ScannedBarcodeCardListener


class ScannedBarcodeCellAdapter(
    var list: ArrayList<ScannedItem?>,
    var listener: ScannedBarcodeCardListener? = null
) :
    RecyclerView.Adapter<ScannedBarcodeViewHolder>() {

    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedBarcodeViewHolder {
        context = parent.context

        val inflater =
            ItemScannedBarcodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScannedBarcodeViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: ScannedBarcodeViewHolder, position: Int) {
        val scannedItem: ScannedItem? = list[position]
        holder.bind(scannedItem)

    }

    override fun getItemCount(): Int = list.size

    fun deleteItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun clearList() {
        val size: Int = list.size
        list.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun insertItem(item: ScannedItem?) {
        list.add(0, item)
        notifyItemChanged(0)
        notifyItemInserted(0)
    }

}

class ScannedBarcodeViewHolder(
    private val binding: ItemScannedBarcodeBinding,
    private var parent: ViewGroup,
    private var mAdapter: ScannedBarcodeCellAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(scannedItem: ScannedItem?) {
        if (scannedItem?.barcodeScanType == BarcodeScanType.PACKAGE_PICKUP) {
            val pkg = scannedItem.data as Package

            binding.itemReceiverName.textItem.text = pkg.getFullReceiverName()
            binding.itemBarcode.textItem.text = pkg.barcode
            binding.itemReceiverCity.textItem.text = pkg.destinationCity
            if (pkg.invoiceNumber != null) {
                binding.itemInvoiceNumber.textItem.text = pkg.invoiceNumber
                binding.itemInvoiceNumber.root.visibility = View.VISIBLE
            } else {
                binding.itemInvoiceNumber.root.visibility = View.GONE
            }

            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.scanned_barcode_context_menu)
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    if (mAdapter.context != null) {
                        when (item?.itemId) {
                            R.id.action_cancel_pickup -> {
                                mAdapter.listener?.onCancelPickup(
                                    adapterPosition,
                                    scannedItem.data as Package
                                )
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
