package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.databinding.ItemFulfilmentOrderItemCellBinding
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_fulfilment_order_item_cell.view.item_card


class FulfilmentOrderItemCellAdapter(
    var productItemsList: ArrayList<ProductItem?>,
    var context: Context?
) :
    RecyclerView.Adapter<FulfilmentOrderItemCellAdapter.FulfilmentOrderItemCellViewHolder>() {

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var highlightedPosition: Int? = null

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): FulfilmentOrderItemCellViewHolder {
        val inflater =
            ItemFulfilmentOrderItemCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return FulfilmentOrderItemCellViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        FulfilmentOrderItemCellViewHolder: FulfilmentOrderItemCellViewHolder,
        position: Int
    ) {
        val productItem: ProductItem? = productItemsList[position]

        if (position == highlightedPosition) {
            FulfilmentOrderItemCellViewHolder.itemView.item_card.setBackgroundResource(R.drawable.highlighted_item_background)
        } else {
            FulfilmentOrderItemCellViewHolder.itemView.item_card.setBackgroundResource(0)
        }

        FulfilmentOrderItemCellViewHolder.setIsRecyclable(false);
        FulfilmentOrderItemCellViewHolder.bind(productItem)
    }

    override fun getItemCount(): Int {
        return productItemsList.size
    }

    fun removeItem(position: Int) {
        productItemsList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun highlightItem(position: Int) {
        if (position >= 0 && position < productItemsList.size) {
            val highlightedItem = productItemsList[position]
            productItemsList.removeAt(position)
            productItemsList.add(0, highlightedItem)
            highlightedPosition = 0
            notifyDataSetChanged()
        }
    }

    fun scanItem(sku: String?): Int {
        for (index in productItemsList.indices) {
            if (productItemsList[index]?.sku == sku) {
                return if (productItemsList[index]?.quantity != null && productItemsList[index]!!.quantity!! > 0) {
                    productItemsList[index]?.quantity = productItemsList[index]!!.quantity?.minus(1)
                    if (productItemsList[index]?.quantity == 0) {
                        removeItem(index)
                    }
                    notifyItemChanged(index)
                    index
                } else {
                    removeItem(index)
                    0
                }
            }
        }
        return 0
    }

    fun getCount(): Int {
        var sum = 0
        for (index in productItemsList.indices) {
            sum += productItemsList[index]?.quantity!!
        }
        return sum
    }


    class FulfilmentOrderItemCellViewHolder(
        private var binding: ItemFulfilmentOrderItemCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: FulfilmentOrderItemCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(productItem: ProductItem?) {

            binding.textQuantity.text = productItem?.quantity.toString()
            binding.itemProductName.textItem.text = productItem?.productName
            binding.itemProductSku.textItem.text = productItem?.sku
            binding.itemBinLocation.textItem.text = productItem?.itemBinLocation
            if(productItem?.productImageUrl != null){
                Picasso.get()
                    .load(productItem.productImageUrl)
                    .into(binding.itemImage)
            }
        }
    }
}