package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Bundles
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemReturnedPackageCustomerCellBinding
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.logestechs.driver.utils.interfaces.ReturnedPackagesCardListener
import com.logestechs.driver.utils.setThrottleClickListener

class ReturnedBundlesCustomerCellAdapter(
    var bundlesList: ArrayList<Bundles?>,
    var context: Context?,
    var listener: ReturnedPackagesCardListener?
) :
    RecyclerView.Adapter<ReturnedBundlesCustomerCellAdapter.BundleViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): BundleViewHolder {
        val inflater =
            ItemReturnedPackageCustomerCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return BundleViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        customerViewHolder: BundleViewHolder,
        position: Int
    ) {
        val customer: Bundles? = bundlesList[position]
        customerViewHolder.bind(customer)
    }

    override fun getItemCount(): Int {
        return bundlesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<Bundles?>) {
        this.bundlesList.clear()
        this.bundlesList.addAll(list)
        this.notifyDataSetChanged()
    }

    fun deleteItem(index: Int) {
        bundlesList.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, bundlesList.size)
    }

    class BundleViewHolder(
        var binding: ItemReturnedPackageCustomerCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: ReturnedBundlesCustomerCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bundle: Bundles?) {
            binding.itemSenderName.textItem.text = bundle?.customerName
            binding.itemSenderAddress.textItem.text = bundle?.cityName

            if (bundle?.barcode != null && bundle.barcode!!.isNotEmpty()) {
                binding.itemBarcode.root.visibility = View.VISIBLE
                binding.itemBarcode.textItem.text = bundle.barcode
            } else {
                binding.itemBarcode.root.visibility = View.GONE
            }

            binding.textCount.text = bundle?.packagesNumber.toString()

            handleCardExpansion(adapterPosition)

            binding.root.setOnClickListener {
                onCardClick(adapterPosition)
            }

            binding.buttonDeliverToSender.setThrottleClickListener({
                mAdapter.listener?.deliverCustomerPackages(adapterPosition)
            })

            val layoutManager = PeekingLinearLayoutManager(
                binding.rvPackages
                    .context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            layoutManager.initialPrefetchItemCount = bundle?.packages?.size ?: 0

            val childItemAdapter = ReturnedBundlesCellAdapter(
                bundle?.packages ?: ArrayList<Package>(),
                mAdapter.context,
                mAdapter.listener,
                adapterPosition
            )
            binding.rvPackages.layoutManager = layoutManager
            binding.rvPackages.adapter = childItemAdapter
        }

        private fun onCardClick(position: Int) {
            if (mAdapter.bundlesList[position]?.isExpanded == true) {
                mAdapter.bundlesList[position]?.isExpanded = false
                binding.rvPackages.visibility = View.GONE
                binding.buttonsContainer.visibility = View.VISIBLE
                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_card_arrow_down_pink
                        )
                    )
                }
            } else {
                mAdapter.listener?.getCustomerPackages(position)
            }
        }

        private fun handleCardExpansion(position: Int) {
            if (mAdapter.bundlesList[position]?.isExpanded == true) {

                binding.rvPackages.visibility = View.VISIBLE
                binding.buttonsContainer.visibility = View.GONE

                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_card_arrow_up_pink
                        )
                    )
                }
            } else {
                binding.rvPackages.visibility = View.GONE
                binding.buttonsContainer.visibility = View.VISIBLE

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