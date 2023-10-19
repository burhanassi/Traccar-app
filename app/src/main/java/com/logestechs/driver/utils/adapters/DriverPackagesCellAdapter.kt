package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemScannedPackageOnShelfBinding
import com.logestechs.driver.utils.SharedPreferenceWrapper

class DriverPackagesCellAdapter(
    var list: ArrayList<Package?>
) : RecyclerView.Adapter<DriverPackagesViewHolder>() {
    var context: Context? = null
    val driverCompanyConfigurations =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DriverPackagesViewHolder {
        context = parent.context

        val inflater =
            ItemScannedPackageOnShelfBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DriverPackagesViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: DriverPackagesViewHolder, position: Int) {
        val scannedItem: Package? = list[position]
        holder.bind(scannedItem)

    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<Package?>) {
        this.list.addAll(list)
        this.notifyDataSetChanged()
    }

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
}

class DriverPackagesViewHolder(
    private val binding: ItemScannedPackageOnShelfBinding,
    private var parent: ViewGroup,
    private var mAdapter: DriverPackagesCellAdapter
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(pkg: Package?) {
        binding.itemReceiverName.textItem.text =
            pkg?.receiverName ?: (pkg?.receiverFirstName + " " + pkg?.receiverLastName)
        binding.itemBarcode.textItem.text = pkg?.barcode
        binding.itemReceiverCity.textItem.text =
            pkg?.destinationAddress?.city ?: pkg?.destinationCity
        binding.itemReceiverPhone.textItem.text = pkg?.receiverPhone

        binding.buttonContextMenu.visibility = View.GONE
    }
}
