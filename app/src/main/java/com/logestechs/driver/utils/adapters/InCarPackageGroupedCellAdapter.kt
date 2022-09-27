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
import com.logestechs.driver.data.model.GroupedPackages
import com.logestechs.driver.databinding.ItemInCarPackageGroupedCellBinding
import com.logestechs.driver.utils.InCarPackagesViewMode
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.logestechs.driver.utils.interfaces.InCarPackagesCardListener

class InCarPackageGroupedCellAdapter(
    var packagesList: ArrayList<GroupedPackages?>,
    var context: Context?,
    var listener: InCarPackagesCardListener?
) :
    RecyclerView.Adapter<InCarPackageGroupedCellAdapter.InCarGroupedPackageViewHolder>() {

    private var selectedViewMode: InCarPackagesViewMode? = null
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
    fun update(list: ArrayList<GroupedPackages?>, selectedViewMode: InCarPackagesViewMode?) {
        this.packagesList.clear()
        this.packagesList.addAll(list)
        this.selectedViewMode = selectedViewMode
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
            if (mAdapter.context != null) {
                when (mAdapter.selectedViewMode) {
                    InCarPackagesViewMode.BY_VILLAGE -> {
                        binding.itemTitle.iconImageView.background = ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_location_pin_gray
                        )
                    }
                    InCarPackagesViewMode.BY_RECEIVER -> {
                        binding.itemTitle.iconImageView.background = ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_receiver_gray
                        )
                    }
                    InCarPackagesViewMode.BY_CUSTOMER -> {
                        binding.itemTitle.iconImageView.background = ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_sender_gray
                        )
                    }
                    else -> {
                        binding.itemTitle.iconImageView.background = ContextCompat.getDrawable(
                            mAdapter.context!!,
                            R.drawable.ic_location_pin_gray
                        )
                    }
                }
            }

            binding.itemTitle.textItem.text = groupedPackage?.label
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
