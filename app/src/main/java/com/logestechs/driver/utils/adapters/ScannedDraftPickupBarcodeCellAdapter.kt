package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.ScannedItem
import com.logestechs.driver.databinding.ItemScannedBarcodeForDraftBinding

class ScannedDraftPickupBarcodeCellAdapter(
    var list: ArrayList<ScannedItem?>,
) :
    RecyclerView.Adapter<ScannedDraftPickupBarcodeViewHolder>() {

    lateinit var mContext: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScannedDraftPickupBarcodeViewHolder {
        mContext = parent.context

        val inflater =
            ItemScannedBarcodeForDraftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ScannedDraftPickupBarcodeViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: ScannedDraftPickupBarcodeViewHolder, position: Int) {
        val scannedItem: ScannedItem? = list[position]
        holder.bind(scannedItem)

    }

    override fun getItemCount(): Int = list.size
    fun clearList() {
        val size: Int = list.size
        list.clear()
        notifyItemRangeRemoved(0, size)
    }
}

class ScannedDraftPickupBarcodeViewHolder(
    private val binding: ItemScannedBarcodeForDraftBinding,
    private var parent: ViewGroup,
    private var mAdapter: ScannedDraftPickupBarcodeCellAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(scannedItem: ScannedItem?) {
        binding.textBarcode.text = scannedItem?.barcode
    }
}
