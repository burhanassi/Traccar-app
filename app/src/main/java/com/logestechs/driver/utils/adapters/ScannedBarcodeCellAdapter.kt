package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.ScannedItem
import com.logestechs.driver.databinding.ItemScannedBarcodeBinding
import com.logestechs.driver.utils.BarcodeScanType


class ScannedBarcodeCellAdapter(
    var list: ArrayList<ScannedItem?>,
) :
    RecyclerView.Adapter<ScannedBarcodeViewHolder>() {

    lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedBarcodeViewHolder {
        mContext = parent.context

        val inflater =
            ItemScannedBarcodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScannedBarcodeViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: ScannedBarcodeViewHolder, position: Int) {
        val scannedItem: ScannedItem? = list[position]
        holder.bind(scannedItem)

    }

    override fun getItemCount(): Int = list.size

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
        }
    }
}
