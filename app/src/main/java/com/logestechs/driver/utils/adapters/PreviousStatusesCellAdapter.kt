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

class PreviousStatusesCellAdapter(
    var previousStatuses: ArrayList<ItemTrackingStatus?>,
    var context: Context?,
) : RecyclerView.Adapter<PreviousStatusesCellAdapter.PreviousStatusesCellViewHolder>() {

    val companyConfigurations: DriverCompanyConfigurations? =
        SharedPreferenceWrapper.getDriverCompanySettings()?.driverCompanyConfigurations

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

        return PreviousStatusesCellAdapter.PreviousStatusesCellViewHolder(inflater, this)
    }

    override fun onBindViewHolder(
        previousStatusesCellViewHolder: PreviousStatusesCellAdapter.PreviousStatusesCellViewHolder,
        position: Int
    ) {
        val status: ItemTrackingStatus? = previousStatuses[position]
        previousStatusesCellViewHolder.setIsRecyclable(false)
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
        private var mAdapter: PreviousStatusesCellAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        private val gradientDrawable = GradientDrawable()

        fun bind(status: ItemTrackingStatus?) {
            if (Lingver.getInstance().getLocale().toString() == AppLanguages.ARABIC.value) {
                binding.itemStatus.text = status?.getStatusText(AppLanguages.ARABIC)
                binding.itemNote.text = status?.arabicNote
            } else {
                binding.itemStatus.text = status?.getStatusText(AppLanguages.ENGLISH)
                binding.itemNote.text = status?.note
            }
            binding.itemCreatedDate.text =
                Helper.formatServerDateLocalized(
                    status?.createdDate,
                    DateFormats.MESSAGE_TEMPLATE_WITH_TIME
                )

            val statusColor = getStatusColor(status)
            gradientDrawable.cornerRadius =
                binding.containerItemStatus.resources.getDimension(R.dimen.corner_radius)
            gradientDrawable.setColor(binding.containerItemStatus.resources.getColor(statusColor))
            binding.containerItemStatus.background = gradientDrawable
        }

        private fun getStatusColor(status: ItemTrackingStatus?): Int {
            return when (status?.status) {
                FulfillmentItemStatus.UNSORTED -> R.color.yellow
                FulfillmentItemStatus.REJECTED -> R.color.red
                FulfillmentItemStatus.SORTED -> R.color.green
                FulfillmentItemStatus.PICKED -> R.color.blue
                FulfillmentItemStatus.PACKED -> R.color.purple
                FulfillmentItemStatus.RETURNED -> R.color.orange
                else -> R.color.white
            }
        }
    }
}
