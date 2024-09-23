package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.api.ApiAdapter
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.ProductItem
import com.logestechs.driver.databinding.ItemFulfilmentOrderItemCellBinding
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class FulfilmentOrderItemCellAdapter(
    var productItemsList: ArrayList<ProductItem?>,
    var context: Context?,
    var orderId: Long? = null
) :
    RecyclerView.Adapter<FulfilmentOrderItemCellAdapter.FulfilmentOrderItemCellViewHolder>() {

    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private var highlightedPosition: Int? = null
    private var quantity: Int? = null

    private var _binding: ItemFulfilmentOrderItemCellBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): FulfilmentOrderItemCellViewHolder {
        _binding =
            ItemFulfilmentOrderItemCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return FulfilmentOrderItemCellViewHolder(binding, viewGroup, this)
    }

    override fun onBindViewHolder(
        FulfilmentOrderItemCellViewHolder: FulfilmentOrderItemCellViewHolder,
        position: Int
    ) {
        val productItem: ProductItem? = productItemsList[position]

        if (position == highlightedPosition) {
            binding.itemCard.setBackgroundResource(R.drawable.highlighted_item_background)
        } else {
            binding.itemCard.setBackgroundResource(0)
        }

        FulfilmentOrderItemCellViewHolder.setIsRecyclable(false);
        FulfilmentOrderItemCellViewHolder.bind(productItem)
    }

    override fun getItemCount(): Int {
        return productItemsList.size
    }

    fun removeItem(position: Int) {
        productItemsList.removeAt(position)
        unHighlightAll()
        notifyItemRemoved(position)
    }

    fun unHighlightAll() {
        highlightedPosition = null
        notifyDataSetChanged()
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
        if (quantity == null) {
            for (index in productItemsList.indices) {
                if (productItemsList[index]?.sku == sku) {
                    return if (productItemsList[index]?.quantity != null && productItemsList[index]!!.quantity!! > 0) {
                        productItemsList[index]?.quantity =
                            productItemsList[index]!!.quantity?.minus(1)
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
        } else {
            for (index in productItemsList.indices) {
                if (productItemsList[index]?.sku == sku) {
                    if (productItemsList[index]?.quantity != null && productItemsList[index]!!.quantity!! > 0) {
                        if (quantity!! < productItemsList[index]?.quantity!!) {
                            productItemsList[index]?.quantity = productItemsList[index]!!.quantity?.minus(
                                quantity!!
                            )
                            if (productItemsList[index]?.quantity == 0) {
                                removeItem(index)
                            }
                            notifyItemChanged(index)
                            return index
                        } else {
                            quantity = quantity!! - productItemsList[index]?.quantity!!
                            productItemsList[index]?.quantity = 0
                            if (quantity == 0) {
                                return index
                            }
                        }
                    } else {
                        removeItem(index)
                        return 0
                    }
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

    fun setQuantity(quantity: Int) {
        this.quantity = quantity
    }

    fun removeZeros() {
        val iterator = productItemsList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item?.quantity == 0) {
                iterator.remove()
            }
        }
        notifyDataSetChanged()
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
            if (productItem?.locationBarcode != null && productItem.locationBarcode!!.isNotEmpty()) {
                binding.itemBinLocation.textItem.text = productItem.locationBarcode
                if (productItem.binBarcode != null && productItem.binBarcode!!.isNotEmpty()) {
                    binding.itemBinLocation.textItem.text = "${productItem.locationBarcode} - ${productItem.binBarcode}"
                }
            } else {
                binding.containerItemBinLocation.visibility = View.GONE
            }
            if (productItem?.expiryDate != null) {
                binding.itemExpiryDate.textItem.text = Helper.formatServerDate(
                    productItem.expiryDate.toString(),
                    DateFormats.MESSAGE_TEMPLATE_WITH_TIME
                )
            } else {
                binding.containerItemExpiryDate.visibility = View.GONE
            }

            if (productItem?.isBundle == true) {
                binding.textTitle.text = mAdapter.context?.getText(R.string.title_bundle)
            }

            if (productItem?.productImageUrl != null && productItem.productImageUrl!!.isNotEmpty()) {
                Picasso.get()
                    .load(productItem.productImageUrl)
                    .into(binding.itemImage)
            }
        }
    }
}