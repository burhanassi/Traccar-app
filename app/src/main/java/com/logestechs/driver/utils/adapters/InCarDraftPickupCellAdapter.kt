package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.data.model.DraftPickup
import com.logestechs.driver.databinding.ItemInCarDraftPickupBinding
import com.logestechs.driver.utils.AppCurrency
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity


class InCarDraftPickupCellAdapter(
    var draftPickupsList: ArrayList<DraftPickup?>,
    var context: Context?
) :
    RecyclerView.Adapter<InCarDraftPickupCellAdapter.InCarDraftPickupViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): InCarDraftPickupViewHolder {
        val inflater =
            ItemInCarDraftPickupBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return InCarDraftPickupViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        ReturnedPackageViewHolder: InCarDraftPickupViewHolder,
        position: Int
    ) {
        val draftPickup: DraftPickup? = draftPickupsList[position]
        ReturnedPackageViewHolder.bind(draftPickup)
    }

    override fun getItemCount(): Int {
        return draftPickupsList.size
    }

    fun removeItem(position: Int) {
        draftPickupsList.removeAt(position)
        notifyDataSetChanged()
    }

    fun clearList() {
        val size: Int = draftPickupsList.size
        draftPickupsList.clear()
        notifyItemRangeRemoved(0, size)
    }

    class InCarDraftPickupViewHolder(
        private var binding: ItemInCarDraftPickupBinding,
        private var parent: ViewGroup,
        private var mAdapter: InCarDraftPickupCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(draftPickup: DraftPickup?) {
            binding.itemSenderName.textItem.text = draftPickup?.customerName
            binding.itemSenderAddress.textItem.text = draftPickup?.address?.toStringAddress()
            binding.textPackagesCount.text = draftPickup?.pickupsNumber.toString()

            binding.imageViewSenderCall.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).callMobileNumber(draftPickup?.customerPhone)
                }
            }

            binding.imageViewSenderSms.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendSms(
                        draftPickup?.customerPhone,
                        Helper.getInterpretedMessageFromTemplate(
                            draftPickup,
                            false,
                            ""
                        )
                    )
                }
            }

            binding.imageViewSenderWhatsApp.setOnClickListener {
                if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                    (mAdapter.context as LogesTechsActivity).sendWhatsAppMessage(
                        Helper.formatNumberForWhatsApp(
                            draftPickup?.customerPhone
                        ), Helper.getInterpretedMessageFromTemplate(
                            draftPickup,
                            false,
                            ""
                        )
                    )
                }
            }

            if (Helper.getCompanyCurrency() == AppCurrency.NIS.value) {
                binding.imageViewSenderWhatsAppSecondary.visibility = View.VISIBLE
                binding.imageViewSenderWhatsAppSecondary.setOnClickListener {
                    if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                        (mAdapter.context as LogesTechsActivity).sendWhatsAppMessage(
                            Helper.formatNumberForWhatsApp(
                                draftPickup?.customerPhone,
                                true
                            ), Helper.getInterpretedMessageFromTemplate(
                                draftPickup,
                                false,
                                ""
                            )
                        )
                    }
                }
            } else {
                binding.imageViewSenderWhatsAppSecondary.visibility = View.GONE
            }

            if (draftPickup?.address != null) {
                if (draftPickup.address!!.latitude != 0.0 || draftPickup.address!!.longitude != 0.0) {
                    binding.imageViewSenderLocation.visibility = View.VISIBLE
                    binding.imageViewSenderLocation.setOnClickListener {
                        if (mAdapter.context != null && mAdapter.context is LogesTechsActivity) {
                            (mAdapter.context as LogesTechsActivity).showLocationInGoogleMaps(
                                draftPickup.address
                            )
                        }
                    }
                } else {
                    binding.imageViewSenderLocation.visibility = View.GONE
                }
            } else {
                binding.imageViewSenderLocation.visibility = View.GONE
            }
        }
    }
}