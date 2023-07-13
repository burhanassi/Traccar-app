package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.databinding.ItemFulfilmentOrderItemCellBinding
import com.logestechs.driver.utils.SharedPreferenceWrapper
import android.os.Handler


class FulfilmentOrderItemCellAdapter(
    var productItemsList: ArrayList<ProductItem?>,
    var context: Context?
) :
    RecyclerView.Adapter<FulfilmentOrderItemCellAdapter.FulfilmentOrderItemCellViewHolder>() {

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

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
        FulfilmentOrderItemCellViewHolder.bind(productItem)
    }

    override fun getItemCount(): Int {
        return productItemsList.size
    }

    fun removeItem(position: Int) {
        productItemsList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun scanItem(sku: String?): Int {
        for (index in productItemsList.indices) {
            if (productItemsList[index]?.sku == sku) {
                return if (productItemsList[index]?.quantity != null && productItemsList[index]!!.quantity!! > 0) {
                    productItemsList[index]?.quantity = productItemsList[index]!!.quantity?.minus(1)
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

            if (productItem?.quantity == 0) {
                Handler().post {
                val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        mAdapter.removeItem(position)
                    }
                }
            }
        }
    }
}