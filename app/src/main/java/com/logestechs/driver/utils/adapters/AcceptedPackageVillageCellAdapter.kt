package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.Customer
import com.logestechs.driver.data.model.Village
import com.logestechs.driver.databinding.ItemAcceptedPackageVillageCellBinding
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.logestechs.driver.utils.interfaces.AcceptedPackagesCardListener

class AcceptedPackageVillageCellAdapter(
    var villagesList: ArrayList<Village?>,
    var context: Context?,
    var fragmentManager: FragmentManager,
    var listener: AcceptedPackagesCardListener,
    var isSprint: Boolean = false
) :
    RecyclerView.Adapter<AcceptedPackageVillageCellAdapter.AcceptedPackageVillageViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): AcceptedPackageVillageViewHolder {
        val inflater =
            ItemAcceptedPackageVillageCellBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return AcceptedPackageVillageViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        AcceptedPackageVillageViewHolder: AcceptedPackageVillageViewHolder,
        position: Int
    ) {
        val village: Village? = villagesList[position]
        AcceptedPackageVillageViewHolder.bind(village)
    }

    override fun getItemCount(): Int {
        return villagesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<Village?>) {
        this.villagesList.clear()
        this.villagesList.addAll(list)
        this.notifyDataSetChanged()
    }

    fun deleteItem(index: Int) {
        villagesList.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, villagesList.size)
    }

    class AcceptedPackageVillageViewHolder(
        var binding: ItemAcceptedPackageVillageCellBinding,
        private var parent: ViewGroup,
        private var mAdapter: AcceptedPackageVillageCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(village: Village?) {
            binding.itemVillageName.textItem.text = village?.name
            binding.textCount.text = village?.numberOfPackages.toString()

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

            layoutManager.initialPrefetchItemCount = village?.customers?.size ?: 0

            val childItemAdapter = AcceptedPackageCustomerCellAdapter(
                village?.customers ?: ArrayList<Customer>(),
                mAdapter.context,
                mAdapter.fragmentManager,
                mAdapter.listener,
                adapterPosition,
                mAdapter.isSprint
            )
            binding.rvPackages.layoutManager = layoutManager
            binding.rvPackages.adapter = childItemAdapter
        }

        private fun onCardClick(position: Int) {
            if (mAdapter.villagesList[position]?.isExpanded == true) {
                mAdapter.villagesList[position]?.isExpanded = false
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
                mAdapter.villagesList[position]?.isExpanded = true
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
            if (mAdapter.villagesList[position]?.isExpanded == true) {

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

