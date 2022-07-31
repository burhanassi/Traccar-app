package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemInCarPackageCellBinding
import com.logestechs.driver.utils.Helper.Companion.format
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener


class InCarPackageCellAdapter(
    var packagesList: ArrayList<Package?>,
    var context: Context?,
    var listener: AcceptedPackagesCardListener?,
    var parentIndex: Int?,
    var isGrouped: Boolean = true
) :
    RecyclerView.Adapter<InCarPackageCellAdapter.AcceptedPackageCustomerCellViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): AcceptedPackageCustomerCellViewHolder {
        val inflater =
            ItemInCarPackageCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        if (isGrouped) {
            inflater.root.layoutParams = ViewGroup.LayoutParams(
                (viewGroup.width * 0.7).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        return AcceptedPackageCustomerCellViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        InCarPackageViewHolder: AcceptedPackageCustomerCellViewHolder,
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

    class AcceptedPackageCustomerCellViewHolder(
        private var binding: ItemInCarPackageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: InCarPackageCellAdapter
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

            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.in_car_package_context_menu)
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    when (item?.itemId) {
                        R.id.action_add_note -> {
//                            mAdapter.listener?.scanForPickup(customer)
                        }
                    }
                    true
                }

                popup.show()
            }
        }
    }
}