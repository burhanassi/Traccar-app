package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.ScannedItem
import com.logestechs.driver.databinding.ItemScannedPackageOnShelfBinding
import com.logestechs.driver.utils.BarcodeScanType
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.interfaces.ScannedPackagesOnShelfCardListener

class ScannedPackagesOnShelfCellAdapter(
    var list: ArrayList<ScannedItem?>,
    var listener: ScannedPackagesOnShelfCardListener?
) :
    RecyclerView.Adapter<ScannedPackagesOnShelfViewHolder>() {
    var context: Context? = null
    val driverCompanyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScannedPackagesOnShelfViewHolder {
        context = parent.context

        val inflater =
            ItemScannedPackageOnShelfBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ScannedPackagesOnShelfViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: ScannedPackagesOnShelfViewHolder, position: Int) {
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

class ScannedPackagesOnShelfViewHolder(
    private val binding: ItemScannedPackageOnShelfBinding,
    private var parent: ViewGroup,
    private var mAdapter: ScannedPackagesOnShelfCellAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(scannedItem: ScannedItem?) {
        var isFlagged = false
        if (scannedItem?.barcodeScanType == BarcodeScanType.PACKAGE_PICKUP) {
            val pkg = scannedItem.data as Package

            binding.itemReceiverName.textItem.text = pkg.receiverName
            binding.itemBarcode.textItem.text = pkg.barcode
            binding.itemReceiverCity.textItem.text =
                pkg.destinationAddress?.city ?: pkg.destinationCity
            binding.itemReceiverPhone.textItem.text = pkg.receiverPhone

            if (mAdapter.listener == null) {
                binding.buttonFlag.visibility = ViewGroup.GONE
            } else {
                binding.buttonFlag.setOnClickListener {
                    if (isFlagged) {
                        binding.buttonFlag.setImageDrawable(
                            ContextCompat.getDrawable(
                                mAdapter.context!!,
                                R.drawable.ic_flag_gray
                            )
                        )
                        mAdapter.listener?.onUnFlagPackage(pkg.id!!)
                        isFlagged = false
                    } else {
                        binding.buttonFlag.setImageDrawable(
                            ContextCompat.getDrawable(
                                mAdapter.context!!,
                                R.drawable.ic_flag_red
                            )
                        )
                        mAdapter.listener?.onShowPackageNoteDialog(pkg)
                        isFlagged = true
                    }
                }
            }
        }
    }
}