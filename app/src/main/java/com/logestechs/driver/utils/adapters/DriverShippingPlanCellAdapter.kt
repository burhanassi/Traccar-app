package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.ShippingPlan
import com.logestechs.driver.databinding.ItemDriverShippingPlanBinding
import com.logestechs.driver.utils.ShippingPlanStatus
import com.logestechs.driver.utils.interfaces.DriverShippingPlanCardListener


class DriverShippingPlanCellAdapter(
    var list: ArrayList<ShippingPlan?>,
    var listener: DriverShippingPlanCardListener?
) :
    RecyclerView.Adapter<DriverShippingPlanItemViewHolder>() {

    var context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DriverShippingPlanItemViewHolder {
        context = parent.context

        val inflater =
            ItemDriverShippingPlanBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DriverShippingPlanItemViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: DriverShippingPlanItemViewHolder, position: Int) {
        val shippingPlan: ShippingPlan? = list[position]
        holder.bind(shippingPlan)

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

    fun insertItem(shippingPlan: ShippingPlan?) {
        list.add(0, shippingPlan)
        notifyItemChanged(0)
        notifyItemInserted(0)
    }

    fun getItem(index: Int): ShippingPlan? {
        return list[index]
    }

}

class DriverShippingPlanItemViewHolder(
    private val binding: ItemDriverShippingPlanBinding,
    private var parent: ViewGroup,
    private var mAdapter: DriverShippingPlanCellAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(shippingPlan: ShippingPlan?) {
        binding.itemBarcode.textItem.text = shippingPlan?.barcode
        binding.itemCustomerName.textItem.text = shippingPlan?.customerName
        binding.itemWarehouseName.textItem.text = shippingPlan?.warehouseName
        binding.textBoxesCount.text = shippingPlan?.numberOfBoxes.toString()

        if (shippingPlan?.shippingPlanStatus == ShippingPlanStatus.ASSIGNED_TO_DRIVER.name) {
            binding.buttonsContainer.visibility = View.VISIBLE
            binding.buttonPickupShippingPlan.setOnClickListener {
                mAdapter.listener?.onPickup(adapterPosition)
            }
        } else {
            binding.buttonsContainer.visibility = View.GONE
        }

        if (shippingPlan?.shippingPlanStatus == ShippingPlanStatus.PICKED_UP.name) {
            binding.buttonContextMenu.visibility = View.VISIBLE
        } else {
            binding.buttonContextMenu.visibility = View.GONE
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
                                shippingPlan
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
