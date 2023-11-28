package com.logestechs.driver.utils.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DriverCompanyConfigurations
import com.logestechs.driver.data.model.ItemTrackingStatus
import com.logestechs.driver.databinding.ItemInventoryItemStatusBinding
import com.logestechs.driver.utils.AppLanguages
import com.logestechs.driver.utils.DateFormats
import com.logestechs.driver.utils.FulfillmentItemStatus
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.SharedPreferenceWrapper
import com.yariksoffice.lingver.Lingver

class PreviousStatusesCellAdapter (
    var previousStatuses: ArrayList<ItemTrackingStatus?>,
    var context: Context?,
) : RecyclerView.Adapter<PreviousStatusesCellAdapter.PreviousStatusesCellViewHolder>(){

    val companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

    private val gradientDrawable = GradientDrawable()

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): PreviousStatusesCellAdapter.PreviousStatusesCellViewHolder {
        val inflater =
            ItemInventoryItemStatusBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

        return PreviousStatusesCellAdapter.PreviousStatusesCellViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        previousStatusesCellViewHolder: PreviousStatusesCellAdapter.PreviousStatusesCellViewHolder,
        position: Int
    ) {
        val status: ItemTrackingStatus? = previousStatuses[position]
        previousStatusesCellViewHolder.setIsRecyclable(false);
        previousStatusesCellViewHolder.bind(status)
    }

    override fun getItemCount(): Int {
        return previousStatuses.size
    }

    fun removeItem(position: Int) {
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(list: ArrayList<ItemTrackingStatus?>) {
        this.previousStatuses.clear()
        this.previousStatuses.addAll(list)
        this.notifyDataSetChanged()
    }
    class PreviousStatusesCellViewHolder(
        private var binding: ItemInventoryItemStatusBinding,
        private var parent: ViewGroup,
        private var mAdapter: PreviousStatusesCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(status: ItemTrackingStatus?) {
            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                binding.itemStatus.text = status?.getStatusText(AppLanguages.ARABIC)
                binding.itemNote.text = status?.arabicNote
            } else {
                binding.itemStatus.text = status?.getStatusText(AppLanguages.ENGLISH)
                binding.itemNote.text = status?.note
            }
            binding.itemCreatedDate.text =  Helper.formatServerDateLocalized(
                status?.createdDate,
                DateFormats.MESSAGE_TEMPLATE_WITH_TIME
            )

            mAdapter.gradientDrawable.cornerRadius = parent.resources.getDimension(R.dimen.corner_radius)
            when (status?.status) {
                FulfillmentItemStatus.UNSORTED -> {
                    mAdapter.gradientDrawable.setColor(parent.resources.getColor(R.color.yellow))
                    binding.containerItemStatus.background = mAdapter.gradientDrawable
                }

                FulfillmentItemStatus.REJECTED -> {
                    mAdapter.gradientDrawable.setColor(parent.resources.getColor(R.color.red))
                    binding.containerItemStatus.background = mAdapter.gradientDrawable
                }

                FulfillmentItemStatus.SORTED -> {
                    mAdapter.gradientDrawable.setColor(parent.resources.getColor(R.color.green))
                    binding.containerItemStatus.background = mAdapter.gradientDrawable
                }

                FulfillmentItemStatus.PICKED -> {
                    mAdapter.gradientDrawable.setColor(parent.resources.getColor(R.color.blue))
                    binding.containerItemStatus.background = mAdapter.gradientDrawable
                }

                FulfillmentItemStatus.PACKED -> {
                    mAdapter.gradientDrawable.setColor(parent.resources.getColor(R.color.purple))
                    binding.containerItemStatus.background = mAdapter.gradientDrawable
                }

                FulfillmentItemStatus.RETURNED -> {
                    mAdapter.gradientDrawable.setColor(parent.resources.getColor(R.color.orange))
                    binding.containerItemStatus.background = mAdapter.gradientDrawable
                }

                else -> {}
            }
        }
    }
}