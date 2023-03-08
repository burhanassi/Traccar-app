package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.ShippingPlan
import com.logestechs.driver.databinding.ItemShippingPlanBinding


class ShippingPlanCellAdapter(
    var list: ArrayList<ShippingPlan?>
) :
    RecyclerView.Adapter<ShippingPlanItemViewHolder>() {

    var context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShippingPlanItemViewHolder {
        context = parent.context

        val inflater =
            ItemShippingPlanBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ShippingPlanItemViewHolder(inflater, parent, this)
    }

    override fun onBindViewHolder(holder: ShippingPlanItemViewHolder, position: Int) {
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

class ShippingPlanItemViewHolder(
    private val binding: ItemShippingPlanBinding,
    private var parent: ViewGroup,
    private var mAdapter: ShippingPlanCellAdapter
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(shippingPlan: ShippingPlan?) {
        binding.itemBarcode.textItem.text = shippingPlan?.barcode
        binding.itemCustomerName.textItem.text = shippingPlan?.customerName
        binding.itemSkuCount.textItem.text = shippingPlan?.numberOfSkus.toString()
        binding.textQuantity.text = shippingPlan?.totalQuantity.toString()
    }
}
