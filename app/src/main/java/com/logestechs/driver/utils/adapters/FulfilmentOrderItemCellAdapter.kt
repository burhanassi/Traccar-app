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
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_fulfilment_order_item_cell.view.item_card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


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
            if (productItem?.itemBinLocation != null && productItem.itemBinLocation!!.isNotEmpty()) {
                binding.itemBinLocation.textItem.text = productItem.itemBinLocation
            } else {
                binding.containerItemBinLocation.visibility = View.GONE
            }
            if (productItem?.expiryDate != null) {
                binding.itemExpiryDate.textItem.text = productItem.expiryDate
            } else {
                binding.containerItemExpiryDate.visibility = View.GONE
            }

            if (productItem?.isBundle == true) {
                binding.textTitle.text = mAdapter.context?.getText(R.string.title_bundle)
                binding.imageArrow.visibility = View.VISIBLE
                handleCardExpansion(adapterPosition)
                binding.root.setOnClickListener {
                    onCardClick(adapterPosition)
                }
            }

            if (productItem?.productImageUrl != null) {
                Picasso.get()
                    .load(productItem.productImageUrl)
                    .into(binding.itemImage)
            }
        }

        private fun onCardClick(position: Int) {
            if (mAdapter.productItemsList[position]?.isExpanded == true) {
                mAdapter.productItemsList[position]?.isExpanded = false
                binding.rvSubBundles.visibility = View.GONE
                binding.containerSubBundleDetails.visibility = View.GONE

                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_card_arrow_down_pink
                        )
                    )
                }
            } else {
                mAdapter.productItemsList[position]?.isExpanded = true
                binding.rvSubBundles.visibility = View.VISIBLE
                binding.containerSubBundleDetails.visibility = View.VISIBLE

                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_card_arrow_up_pink
                        )
                    )
                }

                mAdapter.productItemsList[position]?.let {
                    if (it.isBundle!!) {
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val response = ApiAdapter.apiClient.getSubBundlesProduct(it.id!!)
                                withContext(Dispatchers.Main) {
                                    if (response?.isSuccessful == true && response.body() != null) {
                                        val layoutManager = PeekingLinearLayoutManager(
                                            binding.rvSubBundles.context,
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )

                                        layoutManager.initialPrefetchItemCount =
                                            response.body()!!.size
                                        val childItemAdapter = SubBundleCellAdapter(
                                            response.body()!!,
                                            mAdapter.context
                                        )
                                        binding.rvSubBundles.layoutManager = layoutManager
                                        binding.rvSubBundles.adapter = childItemAdapter
                                    } else {
                                        try {
                                            val jObjError = JSONObject(response?.errorBody()!!.string())
                                            withContext(Dispatchers.Main) {
                                                Helper.showErrorMessage(
                                                    mAdapter.context,
                                                    jObjError.optString(AppConstants.ERROR_KEY)
                                                )
                                            }
                                        } catch (e: java.lang.Exception) {
                                            withContext(Dispatchers.Main) {
                                                Helper.showErrorMessage(
                                                    mAdapter.context,
                                                    mAdapter.context!!.getString(R.string.error_general)
                                                )
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Helper.logException(e, Throwable().stackTraceToString())
                                withContext(Dispatchers.Main) {
                                    if (e.message != null && e.message!!.isNotEmpty()) {
                                        Helper.showErrorMessage(mAdapter.context, e.message)
                                    } else {
                                        Helper.showErrorMessage(mAdapter.context, e.stackTraceToString())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun handleCardExpansion(position: Int) {
            if (mAdapter.productItemsList[position]?.isExpanded == true) {

                binding.rvSubBundles.visibility = View.VISIBLE
                binding.containerSubBundleDetails.visibility = View.VISIBLE

                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_card_arrow_up_pink
                        )
                    )
                }
            } else {
                binding.rvSubBundles.visibility = View.GONE
                binding.containerSubBundleDetails.visibility = View.GONE

                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_card_arrow_down_pink
                        )
                    )
                }
            }
        }
    }
}