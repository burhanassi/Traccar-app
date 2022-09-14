package com.logestechs.driver.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.logestechs.driver.R
import com.logestechs.driver.data.model.DraftPickup
import com.logestechs.driver.databinding.ItemPendingDraftPickupBinding
import com.logestechs.driver.utils.AppCurrency
import com.logestechs.driver.utils.Helper
import com.logestechs.driver.utils.LogesTechsActivity
import com.logestechs.driver.utils.interfaces.PendingDraftPickupCardListener

class PendingDraftPickupCellAdapter(
    var draftPickupsList: ArrayList<DraftPickup?>,
    var context: Context?,
    var listener: PendingDraftPickupCardListener?,
) :
    RecyclerView.Adapter<PendingDraftPickupCellAdapter.PendingDraftPickupViewHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): PendingDraftPickupViewHolder {
        val inflater =
            ItemPendingDraftPickupBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        return PendingDraftPickupViewHolder(inflater, viewGroup, this)
    }

    override fun onBindViewHolder(
        ReturnedPackageViewHolder: PendingDraftPickupViewHolder,
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

    class PendingDraftPickupViewHolder(
        private var binding: ItemPendingDraftPickupBinding,
        private var parent: ViewGroup,
        private var mAdapter: PendingDraftPickupCellAdapter
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(draftPickup: DraftPickup?) {
            binding.itemSenderName.textItem.text = draftPickup?.customerName
            binding.itemSenderAddress.textItem.text = draftPickup?.address?.toStringAddress()

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

            binding.buttonDeliver.setOnClickListener {
                mAdapter.listener?.onAcceptDraftPickup(adapterPosition)
            }

            binding.buttonContextMenu.setOnClickListener {
                val popup = PopupMenu(mAdapter.context, binding.buttonContextMenu)
                popup.inflate(R.menu.pending_package_context_menu)
                popup.setOnMenuItemClickListener { item: MenuItem? ->

                    when (item?.itemId) {
                        R.id.action_reject_package -> {
                            mAdapter.listener?.onRejectDraftPickup(adapterPosition)
                        }
                    }
                    true
                }
                popup.show()
            }
        }
    }
}