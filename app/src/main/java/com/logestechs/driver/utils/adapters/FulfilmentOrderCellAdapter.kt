package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.FulfilmentOrder
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.databinding.ItemFulfilmentOrderCellBinding
import com.logestechs.driver.databinding.ItemFulfilmentOrderItemCellBinding
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.interfaces.FulfilmentOrderCardListener
import com.logestechs.driver.utils.interfaces.NewFulfilmentOrderCardListener
import com.squareup.picasso.Picasso


class FulfilmentOrderCellAdapter(
    var fulfilmentOrderList: ArrayList<FulfilmentOrder?>,
    var listener: FulfilmentOrderCardListener,
    var context: Context?
) :
    RecyclerView.Adapter<FulfilmentOrderCellAdapter.FulfilmentOrderCellViewHolder>() {

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var highlightedPosition: Int? = null

    private var _binding: ItemFulfilmentOrderCellBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): FulfilmentOrderCellViewHolder {
        _binding =
            ItemFulfilmentOrderCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return FulfilmentOrderCellViewHolder(binding, viewGroup, this)
    }

    override fun onBindViewHolder(
        FulfilmentOrderItemCellViewHolder: FulfilmentOrderCellViewHolder,
        position: Int
    ) {
        val productItem: FulfilmentOrder? = fulfilmentOrderList[position]

        if (position == highlightedPosition) {
            binding.itemCard.setBackgroundResource(R.drawable.highlighted_item_background)
        } else {
            binding.itemCard.setBackgroundResource(0)
        }

        FulfilmentOrderItemCellViewHolder.setIsRecyclable(false);
        FulfilmentOrderItemCellViewHolder.bind(productItem)
    }

    override fun getItemCount(): Int {
        return fulfilmentOrderList.size
    }

    fun removeOrder(barcode: String) {
        val position = fulfilmentOrderList.indexOfFirst { it?.barcode == barcode }
        if (position != -1) {
            fulfilmentOrderList.removeAt(position)
            unHighlightAll()
            notifyItemRemoved(position)
        }
    }


    fun unHighlightAll() {
        highlightedPosition = null
        notifyDataSetChanged()
    }

    class FulfilmentOrderCellViewHolder(
        private var binding: ItemFulfilmentOrderCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: FulfilmentOrderCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fulfilmentOrder: FulfilmentOrder?) {

            binding.itemProductName.textItem.text = fulfilmentOrder?.barcode
            binding.buttonDone.setOnClickListener {
                mAdapter.listener.onScanToteForOrder(fulfilmentOrder!!)
            }
        }
    }
}