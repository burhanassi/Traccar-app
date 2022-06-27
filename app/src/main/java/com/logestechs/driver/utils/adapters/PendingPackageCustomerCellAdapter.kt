package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemPendingPackageCustomerCellBinding
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager

class PendingPackageCustomerCellAdapter(
    private var customersList: ArrayList<Customer?>,
    var context: Context?
) :
    RecyclerView.Adapter<PendingPackageCustomerCellAdapter.CustomerViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): CustomerViewHolder {
        val inflater =
            ItemPendingPackageCustomerCellBinding.inflate(
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
        customerViewHolder.setIsRecyclable(false)
        customerViewHolder.bind(customer)
    }

    override fun getItemCount(): Int {
        return customersList.size
    }

    fun update(list: ArrayList<Customer?>) {
        this.customersList.addAll(list)
        this.notifyDataSetChanged()
    }

    class CustomerViewHolder(
        private var binding: ItemPendingPackageCustomerCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: PendingPackageCustomerCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: Customer?) {
            binding.textTitle.text = customer?.firstName
            binding.root.setOnClickListener {
                if (binding.rvPackages.visibility == View.VISIBLE) {
                    binding.rvPackages.visibility = View.GONE
                } else {
                    binding.rvPackages.visibility = View.VISIBLE
                }
            }

            val layoutManager = PeekingLinearLayoutManager(
                binding.rvPackages
                    .context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            layoutManager.initialPrefetchItemCount = customer?.packages?.size ?: 0

            val childItemAdapter = PendingPackageCellAdapter(
                customer?.packages ?: ArrayList<Package>(),
                mAdapter.context
            )
            binding.rvPackages.layoutManager = layoutManager
            binding.rvPackages.adapter = childItemAdapter
            binding.rvPackages
                .setRecycledViewPool(mAdapter.viewPool)
        }
    }
}

