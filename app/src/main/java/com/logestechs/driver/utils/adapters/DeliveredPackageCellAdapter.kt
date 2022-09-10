package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemDeliveredPackageBinding
import com.logestechs.driver.utils.Helper.Companion.format

class DeliveredPackageCellAdapter(
    private var packagesList: ArrayList<Package?>,
    var context: Context?
) :
    RecyclerView.Adapter<DeliveredPackageCellAdapter.DeliveredPackageViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): DeliveredPackageViewHolder {
        val inflater =
            ItemDeliveredPackageBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return DeliveredPackageViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        InCarPackageViewHolder: DeliveredPackageViewHolder,
        position: Int
    ) {
        val pkg: Package? = packagesList[position]
        InCarPackageViewHolder.setIsRecyclable(false);
        InCarPackageViewHolder.bind(pkg)
    }

    override fun getItemCount(): Int {
        return packagesList.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<Package?>) {
        this.packagesList.clear()
        this.packagesList.addAll(list)
        this.notifyDataSetChanged()
    }

    class DeliveredPackageViewHolder(
        private var binding: ItemDeliveredPackageBinding,
        private var parent: ViewGroup,
        private var mAdapter: DeliveredPackageCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pkg: Package?) {
            binding.itemSenderName.textItem.text = pkg?.getFullSenderName()
            binding.itemSenderAddress.textItem.text = pkg?.originAddress?.toStringAddress()

            binding.itemReceiverName.textItem.text = pkg?.getFullReceiverName()
            binding.itemReceiverAddress.textItem.text = pkg?.destinationAddress?.toStringAddress()

            binding.textCod.text = pkg?.cod?.format()

            binding.itemPackageBarcode.textItem.text = pkg?.barcode

            if (pkg?.notes?.trim().isNullOrEmpty()) {
                binding.itemNotes.root.visibility = View.GONE
            } else {
                binding.itemNotes.root.visibility = View.VISIBLE
                binding.itemNotes.textItem.text = pkg?.notes
            }

            if (pkg?.quantity != null && pkg.quantity != 0) {
                binding.itemPackageQuantity.root.visibility = View.VISIBLE
                binding.itemPackageQuantity.textItem.text = pkg.quantity.toString()
            } else {
                binding.itemPackageQuantity.root.visibility = View.GONE
            }

        }
    }
}