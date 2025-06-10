package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.data.model.ShippingPlan
import com.logestechs.driver.databinding.ItemFulfilmentOrderItemCellBinding
import com.logestechs.driver.databinding.ItemFulfilmentOrderItemToPackCellBinding
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.squareup.picasso.Picasso


class FulfilmentOrderItemToPackCellAdapter(
    var productItemsList: ArrayList<ProductItem?>,
    var context: Context?
) :
    RecyclerView.Adapter<FulfilmentOrderItemToPackCellAdapter.FulfilmentOrderItemToPackCellViewHolder>() {

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var highlightedPosition: Int? = null

    private var _binding: ItemFulfilmentOrderItemToPackCellBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): FulfilmentOrderItemToPackCellViewHolder {
        _binding = ItemFulfilmentOrderItemToPackCellBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return FulfilmentOrderItemToPackCellViewHolder(binding, viewGroup, this)
    }


    override fun onBindViewHolder(
        FulfilmentOrderItemToPackCellViewHolder: FulfilmentOrderItemToPackCellViewHolder,
        position: Int
    ) {
        val productItem: ProductItem? = productItemsList[position]

        if (position == highlightedPosition) {
            binding.itemCard.setBackgroundResource(R.drawable.highlighted_item_background)
        } else {
            binding.itemCard.setBackgroundResource(0)
        }

        FulfilmentOrderItemToPackCellViewHolder.setIsRecyclable(false);
        FulfilmentOrderItemToPackCellViewHolder.bind(productItem)
    }

    override fun getItemCount(): Int {
        return productItemsList.size
    }

    fun removeItem(position: Int) {
        productItemsList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun insertItem(item: ProductItem?) {
        productItemsList.add(0, item)
        notifyItemChanged(0)
        notifyItemInserted(0)
    }

    fun clearAllItems() {
        val itemCount = productItemsList.size
        productItemsList.clear()
        notifyItemRangeRemoved(0, itemCount)
    }

    class FulfilmentOrderItemToPackCellViewHolder(
        private var binding: ItemFulfilmentOrderItemToPackCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: FulfilmentOrderItemToPackCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(productItem: ProductItem?) {

            binding.itemProductName.textItem.text = productItem?.productName
            binding.itemProductSku.textItem.text = productItem?.sku
            binding.itemProductBarcode.textItem.text = productItem?.barcode
            if (!productItem?.productImageUrl.isNullOrEmpty()) {
                Picasso.get().load(productItem?.productImageUrl).into(binding.itemImage)
            } else {
                binding.itemImage.setImageResource(R.drawable.ic_item)
            }
            if (productItem?.isCustomPackaging == true && productItem?.parcelTypeName != null) {
                binding.itemNotes.textItem.text = mAdapter.context?.getString(R.string.order_type) +
                        productItem?.parcelTypeName + mAdapter.context?.getString(R.string.text_is_custom_packing)
            } else if (productItem?.isCustomPackaging == true && productItem?.parcelTypeName == null) {
                binding.itemNotes.textItem.text =
                    mAdapter.context?.getString(R.string.text_is_custom_packing)
            } else if (productItem?.isCustomPackaging == false && productItem?.parcelTypeName != null) {
                binding.itemNotes.textItem.text = mAdapter.context?.getString(R.string.order_type) +
                        productItem?.parcelTypeName
            } else {
                binding.containerNotes.visibility = View.GONE
            }
        }
    }
}