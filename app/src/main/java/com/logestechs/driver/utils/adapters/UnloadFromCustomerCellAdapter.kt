package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.ScannedItem
import com.logestechs.driver.databinding.ItemScannedPackageOnShelfBinding
import com.logestechs.driver.utils.BarcodeScanType
import com.logestechs.driver.utils.SharedPreferenceWrapper

class UnloadFromCustomerCellAdapter(
    var list: ArrayList<Package?>
) :
    RecyclerView.Adapter<UnloadFromCustomerViewHolder>() {

    var context: Context? = null
    val driverCompanyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UnloadFromCustomerViewHolder {
        context = parent.context

        val inflater =
            ItemScannedPackageOnShelfBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return UnloadFromCustomerViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: UnloadFromCustomerViewHolder, position: Int) {
        val scannedItem: Package? = list[position]
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

    fun insertItem(item: Package?) {
        list.add(0, item)
        notifyItemChanged(0)
        notifyItemInserted(0)
    }

    fun update(item: Package?) {
        clearList()
        insertItem(item)
    }
}

class UnloadFromCustomerViewHolder(
    private val binding: ItemScannedPackageOnShelfBinding,
    private var parent: ViewGroup,
    private var mAdapter: UnloadFromCustomerCellAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(scannedItem: Package?) {
        binding.itemReceiverName.textItem.text = scannedItem?.receiverName
        binding.itemBarcode.textItem.text = scannedItem?.barcode
        binding.itemReceiverCity.textItem.text =
            scannedItem?.destinationAddress?.city ?: scannedItem?.destinationCity
        binding.itemReceiverPhone.textItem.text = scannedItem?.receiverPhone
        binding.buttonFlag.visibility = ViewGroup.GONE
    }
}