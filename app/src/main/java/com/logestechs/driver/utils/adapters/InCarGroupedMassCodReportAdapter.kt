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
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.GroupedMassCodReports
import com.logestechs.driver.databinding.ItemMassCodReportGroupBinding
import com.logestechs.driver.utils.MassCodReportsViewMode
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.logestechs.driver.utils.customViews.PeekingLinearLayoutManager
import com.logestechs.driver.utils.interfaces.MassCodReportCardListener
import com.logestechs.driver.utils.setThrottleClickListener


class InCarGroupedMassCodReportAdapter(
    var packagesList: ArrayList<GroupedMassCodReports?>,
    var context: Context?,
    var listener: MassCodReportCardListener?
) : RecyclerView.Adapter<InCarGroupedMassCodReportAdapter.InCarGroupedMassCodReportViewHolder>() {

    private var selectedViewMode: MassCodReportsViewMode? = null
    private var companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations
    override fun onCreateViewHolder(
        viewGroup: ViewGroup, i: Int
    ): InCarGroupedMassCodReportViewHolder {
        val inflater = ItemMassCodReportGroupBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return InCarGroupedMassCodReportViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        AcceptedPackageVillageViewHolder: InCarGroupedMassCodReportViewHolder, position: Int
    ) {
        val GroupedMassCodReports: GroupedMassCodReports? = packagesList[position]
        AcceptedPackageVillageViewHolder.bind(GroupedMassCodReports)
    }

    override fun getItemCount(): Int {
        return packagesList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<GroupedMassCodReports?>, selectedViewMode: MassCodReportsViewMode?) {
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

    fun clearList() {
        val size: Int = packagesList.size
        packagesList.clear()
        notifyItemRangeRemoved(0, size)
    }

    class InCarGroupedMassCodReportViewHolder(
        var binding: ItemMassCodReportGroupBinding,
        private var parent: ViewGroup,
        private var mAdapter: InCarGroupedMassCodReportAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(groupedPackage: GroupedMassCodReports?) {

            if (mAdapter.context != null) {
                binding.itemTitle.iconImageView.background = ContextCompat.getDrawable(
                    mAdapter.context!!, R.drawable.ic_sender_gray
                )
            }

            if (mAdapter.companyConfigurations?.isEnablePinCodeForMassCodReportsAndMassReturnedPackages!!) {
                binding.buttonsContainer.visibility = View.GONE
            } else {
                if (groupedPackage?.isExpanded == true) {
                    binding.buttonsContainer.visibility = View.GONE
                } else {
                    binding.buttonsContainer.visibility = View.VISIBLE
                }
            }

            binding.itemTitle.textItem.text = groupedPackage?.customerName
            binding.textCount.text = groupedPackage?.pkgs?.size.toString()
            binding.textCodSum.text = groupedPackage?.codSum.toString()


            handleCardExpansion(adapterPosition)

            binding.containerOvalCount.background = ContextCompat.getDrawable(
                mAdapter.context!!, R.drawable.background_card_semi_circle
            )

            binding.root.setOnClickListener {
                onCardClick(adapterPosition)
            }
            binding.textCodSum.text = groupedPackage?.codSum.toString()
            binding.buttonDeliver.setThrottleClickListener({
                mAdapter.listener?.onDeliverGroupReport(adapterPosition, groupedPackage)
            })

            val layoutManager = PeekingLinearLayoutManager(
                binding.rvPackages.context, LinearLayoutManager.HORIZONTAL, false
            )

            layoutManager.initialPrefetchItemCount = groupedPackage?.pkgs?.size ?: 0

            val childItemAdapter = MassCodReportCellAdapter(
                groupedPackage?.pkgs ?: ArrayList(), mAdapter.context, mAdapter.listener
            )
            binding.rvPackages.layoutManager = layoutManager
            binding.rvPackages.adapter = childItemAdapter
        }

        private fun onCardClick(position: Int) {
            mAdapter.notifyItemChanged(position)
            val groupedPackage = mAdapter.packagesList[position]
            groupedPackage?.isExpanded = !groupedPackage?.isExpanded!!

            if (groupedPackage.isExpanded == true) {
                binding.rvPackages.visibility = View.VISIBLE
                binding.containerOvalCount.background = null
                if (!mAdapter.companyConfigurations?.isEnablePinCodeForMassCodReportsAndMassReturnedPackages!!) {
                    binding.buttonsContainer.visibility = View.GONE
                }
                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!, R.drawable.ic_card_arrow_up_pink
                        )
                    )
                }
            } else {
                binding.rvPackages.visibility = View.GONE
                binding.containerOvalCount.background = ContextCompat.getDrawable(
                    mAdapter.context!!, R.drawable.background_card_semi_circle
                )
                if (!mAdapter.companyConfigurations?.isEnablePinCodeForMassCodReportsAndMassReturnedPackages!!) {
                    binding.buttonsContainer.visibility = View.VISIBLE
                }
                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!, R.drawable.ic_card_arrow_down_pink
                        )
                    )
                }
            }
        }


        private fun handleCardExpansion(position: Int) {
            val groupedPackage = mAdapter.packagesList[position]
            if (groupedPackage?.isExpanded == true) {
                binding.rvPackages.visibility = View.VISIBLE
                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!, R.drawable.ic_card_arrow_up_pink
                        )
                    )
                }
            } else {
                binding.rvPackages.visibility = View.GONE
                if (mAdapter.context != null) {
                    binding.imageArrow.setImageDrawable(
                        ContextCompat.getDrawable(
                            mAdapter.context!!, R.drawable.ic_card_arrow_down_pink
                        )
                    )
                }
            }
        }

    }
}
