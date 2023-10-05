package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.data.model.ScannedItem
import com.logestechs.driver.databinding.ItemScannedBarcodeBinding
import com.logestechs.driver.utils.BarcodeScanType
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.interfaces.ScannedBarcodeCardListener


class ScannedBarcodeCellAdapter(
    var list: ArrayList<ScannedItem?>,
    var listener: ScannedBarcodeCardListener? = null,
    var isSubBundles: Boolean = false
) :
    RecyclerView.Adapter<ScannedBarcodeViewHolder>() {

    var context: Context? = null
    val driverCompanyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

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

    fun makePackageSelected(barcode: String?): Boolean {
        for (i in 0 until list.size) {
            val existingPackage = list[i]?.data as Package
            if (existingPackage.barcode == barcode) {
                existingPackage.isSelected = true
                notifyItemChanged(i)
                return true
            }
        }

        for (i in 0 until list.size) {
            val existingPackage = list[i]?.data as Package
            if (existingPackage.invoiceNumber == barcode) {
                existingPackage.isSelected = true
                notifyItemChanged(i)
                return true
            }
        }
        return false
    }

    fun getSelectedPackagesIds(): List<Long?> {
        val selectedPackages = list.filter { (it?.data as Package).isSelected }
        return selectedPackages.map { it?.id }
    }

    fun insertSubPackage(scannedItem: ScannedItem?, isParent: Boolean) {
        for (index in list.indices) {
            if (list[index]?.barcode == scannedItem?.barcode) {
                if (isParent) {
                    return
                } else {
                    (list[index]?.data as Package).scannedSubPackagesCount += 1
                }
                notifyItemChanged(index)
                return
            }
        }
        if (!isParent) {
            (scannedItem?.data as Package).scannedSubPackagesCount += 1
        }
        list.add(0, scannedItem)
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

            binding.itemReceiverName.textItem.text = pkg.receiverName
            binding.itemBarcode.textItem.text = pkg.barcode
            binding.itemReceiverCity.textItem.text = pkg.destinationAddress?.city
            if (pkg.invoiceNumber != null) {
                binding.itemInvoiceNumber.textItem.text = pkg.invoiceNumber
                binding.itemInvoiceNumber.root.visibility = View.VISIBLE
            } else {
                binding.itemInvoiceNumber.root.visibility = View.GONE
            }

            if (mAdapter.driverCompanyConfigurations?.isPrintAwbCopiesAsPackageQuantity == true && (pkg.quantity
                    ?: 0) > 1
            ) {
                binding.itemPackageQuantity.root.visibility = View.VISIBLE
                binding.itemPackageQuantity.textItem.text = mAdapter.context?.getString(
                    R.string.scanned_sub_packages_count,
                    pkg.scannedSubPackagesCount.toString(),
                    pkg.quantity.toString()
                )
            } else {
                binding.itemPackageQuantity.root.visibility = View.GONE
            }

            if (mAdapter.isSubBundles) {
                binding.buttonContextMenu.visibility = View.GONE
                if (mAdapter.context != null) {
                    if (pkg.isSelected) {
                        binding.viewBackground.background = ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.background_oval_orange
                        )
                    } else {
                        binding.viewBackground.background = ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.background_oval_white
                        )
                    }
                }
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
