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
import com.logestechs.driver.data.model.GroupedPackages
import com.logestechs.driver.data.model.Package
import com.logestechs.driver.databinding.ItemInCarPackageGroupedCellBinding
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener

class InCarPackageGroupedCellAdapter(
    var packagesList: ArrayList<GroupedPackages?>,
    var context: Context?,
    var listener: AcceptedPackagesCardListener?
) :
    RecyclerView.Adapter<InCarPackageGroupedCellAdapter.InCarGroupedPackageViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): InCarGroupedPackageViewHolder {
        val inflater =
            ItemInCarPackageGroupedCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return InCarGroupedPackageViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        AcceptedPackageVillageViewHolder: InCarGroupedPackageViewHolder,
        position: Int
    ) {
        val groupedPackages: GroupedPackages? = packagesList[position]
        AcceptedPackageVillageViewHolder.bind(groupedPackages)
    }

    override fun getItemCount(): Int {
        return packagesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<GroupedPackages?>) {
        this.packagesList.clear()
        this.packagesList.addAll(list)
        this.notifyDataSetChanged()
    }

    fun deleteItem(index: Int) {
        packagesList.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, packagesList.size)
    }

    class InCarGroupedPackageViewHolder(
        var binding: ItemInCarPackageGroupedCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: InCarPackageGroupedCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(groupedPackage: GroupedPackages?) {
            binding.itemVillageName.textItem.text = groupedPackage?.label
            binding.textCount.text = groupedPackage?.pkgs?.size.toString()

            handleCardExpansion(adapterPosition)

            binding.containerOvalCount.background = ContextCompat.getDrawable(
                mAdapter.context!!,
                R.drawable.background_card_semi_circle
            )

            binding.root.setOnClickListener {
                onCardClick(adapterPosition)
            }

            val layoutManager = PeekingLinearLayoutManager(
                binding.rvPackages
                    .context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            layoutManager.initialPrefetchItemCount = groupedPackage?.pkgs?.size ?: 0

            val childItemAdapter = InCarPackageCellAdapter(
                groupedPackage?.pkgs ?: ArrayList(),
                mAdapter.context,
                mAdapter.listener,
                adapterPosition
            )
            binding.rvPackages.layoutManager = layoutManager
            binding.rvPackages.adapter = childItemAdapter
        }

        private fun onCardClick(position: Int) {
            if (mAdapter.packagesList[position]?.isExpanded == true) {
                mAdapter.packagesList[position]?.isExpanded = false
                binding.rvPackages.visibility = View.GONE
                binding.containerOvalCount.background = ContextCompat.getDrawable(
                    mAdapter.context!!,
                    R.drawable.background_card_semi_circle
                )

                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_card_arrow_down_pink
                        )
                    )
                }
            } else {
                mAdapter.packagesList[position]?.isExpanded = true
                binding.rvPackages.visibility = View.VISIBLE
                binding.containerOvalCount.background = null

                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_card_arrow_up_pink
                        )
                    )
                }
            }
        }

        private fun handleCardExpansion(position: Int) {
            if (mAdapter.packagesList[position]?.isExpanded == true) {

                binding.rvPackages.visibility = View.VISIBLE

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
