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
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemReturnedPackageCustomerCellBinding
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.logestechs.driver.utils.interfaces.ReturnedPackagesCardListener
import com.logestechs.driver.utils.setThrottleClickListener

class ReturnedPackageCustomerCellAdapter(
    var customersList: ArrayList<Customer?>,
    var context: Context?,
    var listener: ReturnedPackagesCardListener?
) :
    RecyclerView.Adapter<ReturnedPackageCustomerCellAdapter.CustomerViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): CustomerViewHolder {
        val inflater =
            ItemReturnedPackageCustomerCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return CustomerViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        customerViewHolder: CustomerViewHolder,
        position: Int
    ) {
        val customer: Customer? = customersList[position]
        customerViewHolder.bind(customer)
    }

    override fun getItemCount(): Int {
        return customersList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<Customer?>) {
        this.customersList.clear()
        this.customersList.addAll(list)
        this.notifyDataSetChanged()
    }

    fun deleteItem(index: Int) {
        customersList.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, customersList.size)
    }

    class CustomerViewHolder(
        var binding: ItemReturnedPackageCustomerCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: ReturnedPackageCustomerCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: Customer?) {
            binding.itemSenderName.textItem.text = customer?.customerName
            binding.itemSenderAddress.textItem.text = customer?.city

            if (customer?.massReturnedPackagesReportBarcode != null && customer.massReturnedPackagesReportBarcode.isNotEmpty()) {
                binding.itemBarcode.root.visibility = View.VISIBLE
                binding.itemBarcode.textItem.text = customer.massReturnedPackagesReportBarcode
            } else {
                binding.itemBarcode.root.visibility = View.GONE
            }

            binding.textCount.text = customer?.packagesNumber.toString()

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

            layoutManager.initialPrefetchItemCount = customer?.packages?.size ?: 0

            val childItemAdapter = ReturnedPackageCellAdapter(
                customer?.packages ?: ArrayList<Package>(),
                mAdapter.context,
                mAdapter.listener,
                adapterPosition
            )
            binding.rvPackages.layoutManager = layoutManager
            binding.rvPackages.adapter = childItemAdapter
        }

        private fun onCardClick(position: Int) {
            if (mAdapter.customersList[position]?.isExpanded == true) {
                mAdapter.customersList[position]?.isExpanded = false
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
            if (mAdapter.customersList[position]?.isExpanded == true) {

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

